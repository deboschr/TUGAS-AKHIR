public void start(String seedUrl) {
   isStopped = false;
   repositories.clear();
   rateLimiters.clear();
   frontier.clear();
   if (executor != null) executor.shutdownNow();
   executor = Executors.newVirtualThreadPerTaskExecutor();
   rootHost = UrlHandler.getHost(seedUrl);
   frontier.offer(new Link(seedUrl));
   while (!isStopped && !frontier.isEmpty() && repositories.size() < MAX_LINKS) {
      Link currLink = frontier.poll();
      if (currLink == null) return;

      Document html = checkLink(currLink, true);

      if (!currLink.isWebpage() || html == null) continue;

      Map<Link, String> linksOnWebpage = extractLink(html);

      List<Callable<Void>> tasks = new ArrayList<>();
      for (var entry : linksOnWebpage.entrySet()) {
         if (isStopped) return;

         Link link = entry.getKey();
         String anchorText = entry.getValue();

         Link existingLink = repositories.get(link.getUrl());
         if (existingLink != null) {
            existingLink.addWebpageSource(currLink, anchorText);
            continue;
         }
         link.addWebpageSource(currLink, anchorText);

         if (UrlHandler.getHost(link.getUrl()).equalsIgnoreCase(rootHost)) {
            frontier.offer(link);
         } else {
            tasks.add(() -> {
               checkLink(link, false);
               return null;
            });
         }
      }
      if (!tasks.isEmpty()) {
         try {
            executor.invokeAll(tasks);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }
}
