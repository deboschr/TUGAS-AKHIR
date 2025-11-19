public void start(String seedUrl) {
   isStopped = false;
   repositories.clear();
   frontier.clear();
   rateLimiters.clear();
   rootHost = UrlHandler.getHost(seedUrl);
   frontier.offer(new Link(seedUrl));
   while (!isStopped && !frontier.isEmpty()) {
      if (repositories.size() >= MAX_LINKS) {
         frontier.clear();
         break;
      }

      Link currLink = frontier.poll();
      if (currLink == null) continue;

      Link existing = repositories.putIfAbsent(currLink.getUrl(), currLink);
      if (existing != null) continue;

      Document doc = fetchLink(currLink, true);

      send(currLink);

      if (!currLink.getError().isEmpty()) continue;

      String finalUrlHost = UrlHandler.getHost(currLink.getFinalUrl());
      if (doc == null || !finalUrlHost.equalsIgnoreCase(rootHost)) continue;

      currLink.setIsWebpage(true);

      Map<Link, String> linksOnWebpage = extractLink(doc);
      try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
         for (var entry : linksOnWebpage.entrySet()) {
            if (repositories.size() >= MAX_LINKS || isStopped) {
               frontier.clear();
               break;
            }

            Link link = entry.getKey();
            String anchorText = entry.getValue();

            Link existingLink = repositories.get(link.getUrl());
            if (existingLink != null) {
               existingLink.addConnection(currLink, anchorText);
               continue;
            }

            link.addConnection(currLink, anchorText);

            String host = UrlHandler.getHost(link.getUrl());
            if (host.equalsIgnoreCase(rootHost)) {
               frontier.offer(link);
            } else {
               repositories.putIfAbsent(link.getUrl(), link);
               executor.submit(() -> {
                  RateLimiter limiter = rateLimiters.computeIfAbsent(host, h -> new RateLimiter());
                  limiter.delay();

                  fetchLink(link, false);
                  send(link);
               });
            }
         }

         executor.shutdown();
         executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }
}