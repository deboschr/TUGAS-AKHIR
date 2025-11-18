private Document fetchLink(Link link, boolean isParseDoc) {

   try {
      // variabel buat nyimpen hasil response dari server
      HttpResponse<?> res;

      /*
       * Kalau kita memang butuh parse HTML maka SELALU pakai GET. GET wajib dipakai
       * karena kita butuh isi body-nya (HTML)
       */
      if (isParseDoc) {
         HttpRequest request = HttpRequest.newBuilder()
               // URL tujuan
               .uri(URI.create(link.getUrl()))
               // Header biar server tahu yg minta request adalah aplikasi kita
               .header("User-Agent", USER_AGENT)
               // Timeout total request (connect + read)
               .timeout(Duration.ofSeconds(10))
               // Pakai GET karena butuh body HTML lengkap
               .GET()
               // Build objek HttpRequest
               .build();

         // Kirim request & baca seluruh body sebagai String
         res = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      }
      /*
       * Kalau TIDAK perlu parse maka coba HEAD dulu, pake HEAD karena lebih cepet
       * karena tidak download body
       */
      else {
         try {
            HttpRequest headReq = HttpRequest.newBuilder()
                  // URL target
                  .uri(URI.create(link.getUrl()))
                  // Header biar server tahu yg minta request adalah aplikasi kita
                  .header("User-Agent", USER_AGENT)
                  // Timeout total request (connect + read)
                  .timeout(Duration.ofSeconds(10))
                  // Method HEAD dg hanya minta header, tanpa body
                  .method("HEAD", HttpRequest.BodyPublishers.noBody())
                  // Build objek HttpRequest
                  .build();

            // Kirim HEAD request, body dibuang (discard)
            res = HTTP_CLIENT.send(headReq, HttpResponse.BodyHandlers.discarding());
         }
         /*
          * Kalau HEAD gagal (server tidak support, SSL problem, dll) maka kita fallback
          * ke GET, tapi tetap discard body biar cepat
          */ catch (Exception headError) {
            HttpRequest getReq = HttpRequest
                  .newBuilder()
                  // URL target
                  .uri(URI.create(link.getUrl()))
                  // Header biar server tahu yg minta request adalah aplikasi kita
                  .header("User-Agent", USER_AGENT)
                  // Timeout total request (connect + read)
                  .timeout(Duration.ofSeconds(10))
                  // Method GET
                  .GET()
                  // Build objek HttpRequest
                  .build();

            // GET dipakai, tapi body langsung dibuang (nggak dibaca)
            res = HTTP_CLIENT.send(getReq, HttpResponse.BodyHandlers.discarding());
         }
      }

      // Kode status HTTP (contoh 200, 404, 500, dll.)
      int statusCode = res.statusCode();
      // URL final setelah redirect (kalau ada)
      String finalUrl = res.uri().toString();
      // Ambil header Content-Type (bisa kosong kalau server tidak kirim)
      String contentType = res.headers().firstValue("Content-Type").orElse("").toLowerCase();

      Document doc = null;

      /*
       * Parse body response hanya kalau:
       * - diminta untuk parsing
       * - request-nya oke
       * - response body-nya HTML
       */
      if (isParseDoc && statusCode == 200 && contentType.contains("text/html")) {

         // body sudah String karena kita pakai BodyHandlers.ofString() di mode parse
         String body = (String) res.body();

         try {
            // Jsoup parse body string ke dokumen HTML
            doc = Jsoup.parse(body, finalUrl);
         } catch (Exception ignore) {
            doc = null;
         }
      }

      link.setFinalUrl(finalUrl); // simpan final URL
      link.setContentType(contentType); // simpan tipe konten
      link.setStatusCode(statusCode); // set status (dan otomatis set error message)

      // bisa null kalau bukan HTML atau error
      return doc;

   } catch (Throwable e) {
      // Ambil nama error/class (misal "IOException")
      String errorName = e.getClass().getSimpleName();

      if (errorName.isBlank()) {
         errorName = "UnknownError";
      }

      // simpan nama error
      link.setError(errorName);

      return null;
   }
}
