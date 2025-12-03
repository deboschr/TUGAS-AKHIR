public void start(String seedUrl) {
   isStopped = false;
   repositories.clear();
   rateLimiters.clear();
   frontier.clear();

   rootHost = URLHandler.getHost(seedUrl);
   frontier.offer(new Link(seedUrl));

   while (!isStopped && !frontier.isEmpty() && repositories.size() < MAX_LINKS) {
      Link currLink = frontier.poll();
      if (currLink == null) continue;

      Document html = fetchLink(currLink, true);

      if (!currLink.isWebpage() || html == null) continue;

      Map<Link, String> linksOnWebpage = extractLink(html);

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
               existingLink.addRelation(currLink, anchorText);
               continue;
            }
            link.addRelation(currLink, anchorText);

            if (URLHandler.getHost(link.getUrl()).equalsIgnoreCase(rootHost)) {
               frontier.offer(link);
            } else {
               executor.submit(() -> {
                  fetchLink(link, false);
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
