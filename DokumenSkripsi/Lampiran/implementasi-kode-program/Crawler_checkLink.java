private Document checkLink(Link link, boolean isParseDoc) {
   try {
      RateLimiter limiter = rateLimiters.computeIfAbsent(UrlHandler.getHost(link.getUrl()), h -> new RateLimiter());
      limiter.delay();
      HttpResponse<?> res = HttpHandler.fetch(link.getUrl(), isParseDoc);
      link.setFinalUrl(res.uri().toString());
      link.setContentType(res.headers().firstValue("Content-Type").orElse("").toLowerCase());
      link.setStatusCode(res.statusCode());
      Document doc = null;
      if (isParseDoc && res.body() != null && link.getStatusCode() == 200
            && UrlHandler.getHost(link.getFinalUrl()).equals(rootHost)) {
         String body = (String) res.body();
         try {
            doc = Jsoup.parse(body, link.getFinalUrl());
            link.setIsWebpage(true);
         } catch (Exception ignore) {
            doc = null;
         }
      }
      return doc;
   } catch (Throwable e) {
      link.setError(e.getClass().getSimpleName() || "UnknownError");
      return null;
   } finally {
      if (linkSender != null) Platform.runLater(() -> linkSender.accept(link));
   }
}
