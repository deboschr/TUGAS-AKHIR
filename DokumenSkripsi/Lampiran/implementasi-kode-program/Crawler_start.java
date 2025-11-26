public void start(String seedUrl) {
   isStopped = false;
   repositories.clear();
   rateLimiters.clear();
   frontier.clear();

   rootHost = URLHandler.getHost(seedUrl);
   frontier.offer(new Link(seedUrl));

   while (!isStopped && !frontier.isEmpty() && repositories.size() < MAX_LINKS) {

      Link currLink = frontier.poll();

      if (currLink == null) {
         continue;
      }

      Link existing = repositories.putIfAbsent(currLink.getUrl(), currLink);
      if (existing != null) {
         continue;
      }

      Document doc = fetchLink(currLink, true);

      send(currLink);

      if (!URLHandler.getHost(currLink.getFinalUrl()).equals(rootHost) ||
         !currLink.getError().isEmpty() || 
         doc == null) {
         continue;
      }

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

            if (URLHandler.getHost(link.getUrl()).equals(rootHost)) {
               frontier.offer(link);
            } else {
               repositories.putIfAbsent(link.getUrl(), link);
               executor.submit(() -> {
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
