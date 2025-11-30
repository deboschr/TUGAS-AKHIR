HTTPHandler_fetchpResponse<?> fetch(String url, boolean needResponseBody) throws Exception {
   HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(30)).build();
   HttpResponse<?> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

   int statusCode = response.statusCode();
   String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase();

   if (needResponseBody && statusCode == 200 && contentType.contains("text/html")) {
      response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
   }

   return response;
}