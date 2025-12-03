private Document checkLink(Link link, boolean isParseDoc) {
   try {
      Link existingLink = repositories.get(link.getUrl());
      if (existingLink != null || repositories.size() > MAX_LINKS) {
         return null
      };

      RateLimiter limiter = rateLimiters.computeIfAbsent(UrlHandler.getHost(link.getUrl()), h -> new RateLimiter());
      limiter.delay();

      HttpResponse<?> res = HttpHandler.fetch(link.getUrl(), isParseDoc);
      
      link.setFinalUrl(res.uri().toString());
      link.setContentType(res.headers().firstValue("Content-Type").orElse("").toLowerCase());
      link.setStatusCode(res.statusCode());

      
      Document html = null;
      
      boolean isOk = link.getStatusCode() == 200 && res.body() != null;
      boolean isSameHost = UrlHandler.getHost(link.getFinalUrl()).equalsIgnoreCase(rootHost);
      
      if (isParseDoc && isOk && isSameHost) {
         String body = (String) res.body();

         try {
            html = Jsoup.parse(body, link.getFinalUrl());

            link.setIsWebpage(true);
         } catch (Exception ignore) {
            html = null;
         }
      }

      return html;
   } catch (Throwable e) {
      link.setError(e.getClass().getSimpleName());
      return null;
   } finally {
      Link existingLink = repositories.putIfAbsent(link.getUrl(), link);
      if (existingLink == null) {
         Platform.runLater(() -> linkSender.accept(link));
      }
   }
}
