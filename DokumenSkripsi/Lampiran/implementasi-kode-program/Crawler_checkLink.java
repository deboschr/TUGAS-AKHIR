private Document checkLink(Link link, boolean isParseDoc) {
   try {
      RateLimiter limiter = rateLimiters.computeIfAbsent(URLHandler.getHost(link.getUrl()), h -> new RateLimiter());
      limiter.delay();
      HttpResponse<?> res = HTTPHandler.fetch(link.getUrl(), isParseDoc);
      link.setFinalUrl(res.uri().toString());
      link.setContentType(res.headers().firstValue("Content-Type").orElse("").toLowerCase());
      link.setStatusCode(res.statusCode());
      Document doc = null;
      if (isParseDoc && res.body() != null && link.getStatusCode() == 200 && !URLHandler.getHost(link.getFinalUrl()).equals(rootHost)) {
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
      String errorName = e.getClass().getSimpleName();
      if (errorName.isBlank()) {
         errorName = "UnknownError";
      }
      link.setError(errorName);
      return null;
   }
}