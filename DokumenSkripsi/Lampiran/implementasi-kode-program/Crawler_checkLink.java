private Document checkLink(Link link, boolean isParseDoc) {
   if (repositories.get(link.getUrl()) != null || repositories.size() > MAX_LINKS) return null;
   
   try {
      RateLimiter limiter = rateLimiters.computeIfAbsent(UrlHandler.getHost(link.getUrl()), h -> new RateLimiter());
      limiter.delay();
      
      HttpRequest req = HttpRequest.newBuilder().uri(URI.create(link.getUrl())).GET().header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(REQUEST_TIMEOUT)).build();
      
      HttpResponse<?> res;
      if (isParseDoc) {
         res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
      } else {
         res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.discarding());
      }
      
      link.setFinalUrl(res.uri().toString());
      link.setContentType(res.headers().firstValue("Content-Type").orElse("").toLowerCase());
      link.setStatusCode(res.statusCode());
      
      Document html = null;   
      boolean isFetchOk = link.getStatusCode() == 200 && res.body() != null;
      boolean isSameHost = UrlHandler.getHost(link.getFinalUrl()).equalsIgnoreCase(rootHost);
      
      if (isParseDoc && isFetchOk && isSameHost) {
         try {
            String body = (String) res.body();
            html = Jsoup.parse(body, link.getFinalUrl());
            link.setIsWebpage(true);
         } catch (Exception ignore) {
            html = null;
         }
      }

      return html;
   } catch (Throwable e) {
      link.setError(ErrorHandler.getExceptionError(e));
      return null;
   } finally {
      Link existingLink = repositories.putIfAbsent(link.getUrl(), link);
      if (existing == null) {
         receiver.receive(link);
      }
   }
}
