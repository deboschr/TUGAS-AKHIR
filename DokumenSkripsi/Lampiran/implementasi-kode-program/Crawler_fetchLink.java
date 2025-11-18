private Document fetchLink(Link link, boolean isParseDoc) {
   try {
      HttpResponse<?> res;
      URI url = new URI(link.getUrl());

      if (isParseDoc) {
         HttpRequest request = HttpRequest.newBuilder().uri(url).header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(10)).GET().build();

         res = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      } else {
         try {
            HttpRequest headReq = HttpRequest.newBuilder().uri(url).header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(10)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();

            res = HTTP_CLIENT.send(headReq, HttpResponse.BodyHandlers.discarding());
         } catch (Exception headError) {
            HttpRequest getReq = HttpRequest.newBuilder().uri(url).header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(10)).GET().build();

            res = HTTP_CLIENT.send(getReq, HttpResponse.BodyHandlers.discarding());
         }
      }

      int statusCode = res.statusCode();
      String finalUrl = res.uri().toString();
      String contentType = res.headers().firstValue("Content-Type").orElse("").toLowerCase();

      Document doc = null;
      if (isParseDoc && statusCode == 200 && contentType.contains("text/html")) {
         String body = (String) res.body();
         try {
            doc = Jsoup.parse(body, finalUrl);
         } catch (Exception ignore) {
            doc = null;
         }
      }

      link.setFinalUrl(finalUrl);
      link.setContentType(contentType);
      link.setStatusCode(statusCode);

      return doc;
   } catch (Throwable e) {
      String errorName = e.getClass().getSimpleName();
      if (errorName.isBlank()) errorName = "UnknownError";
      link.setError(errorName);
      return null;
   }
}