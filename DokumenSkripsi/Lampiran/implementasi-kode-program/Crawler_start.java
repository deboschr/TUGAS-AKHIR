public void start(String seedUrl) {
   // Reset status stop (kalau sebelumnya pernah dihentikan user)
   isStopped = false;

   // Reset semua struktur data
   repositories.clear();
   frontier.clear();
   rateLimiters.clear();

   // Ambil host dari seed URL buat identifikasi same-host
   rootHost = UrlHandler.getHost(seedUrl);

   // Masukkan seed ke frontier sebagai titik awal BFS
   frontier.offer(new Link(seedUrl));

   // Loop BFS: selama user belum stop dan masih ada link (webpage link) di
   // frontier
   while (!isStopped && !frontier.isEmpty()) {

      // Kalau sudah mencapai limit global, hentikan BFS
      if (repositories.size() >= MAX_LINKS) {
         frontier.clear();
         break;
      }

      // Ambil link paling depan (FIFO)
      Link currLink = frontier.poll();
      if (currLink == null) {
         continue;
      }

      // Cek apakah URL ini sudah pernah dicatat di repositories
      Link existing = repositories.putIfAbsent(currLink.getUrl(), currLink);
      if (existing != null) {
         // Kalau sudah ada, berarti URL ini pernah dicek maka skip
         continue;
      }

      // Fetch dan parse body dari webpage link (kalau HTML)
      Document doc = fetchLink(currLink, true);

      // Kirim hasil ke controller (apapun hasilnya: sukses / error)
      send(currLink);

      /*
       * Kalau ada error (exception, timeout, dll):
       * - currLink dianggap sebagai broken link
       * - Tidak diperlakukan sebagai webpage yang bisa di-crawling lagi.
       */
      if (!currLink.getError().isEmpty()) {
         continue;
      }

      /*
       * Skip kalau:
       * - doc == null (bukan HTML, misalnya PDF / image / dll)
       * - host dari finalUrl beda dengan host seedUrl (redirect ke domain lain)
       */
      String finalUrlHost = UrlHandler.getHost(currLink.getFinalUrl());
      if (doc == null || !finalUrlHost.equalsIgnoreCase(rootHost)) {
         continue;
      }

      // Kalau sampai sini, berarti currLink adalah webpage link yang valid
      currLink.setIsWebpage(true);

      // Ekstrak seluruh link dari webpage
      Map<Link, String> linksOnWebpage = extractLink(doc);

      // Executor berbasis virtual thread
      try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

         for (var entry : linksOnWebpage.entrySet()) {

            // Kalau limit sudah tercapai, hentikan BFS dengan mengosongkan frontier
            if (repositories.size() >= MAX_LINKS || isStopped) {
               frontier.clear();
               break;
            }

            Link link = entry.getKey();
            String anchorText = entry.getValue();

            // Cek apakah URL ini sudah pernah tercatat di repositories
            Link existingLink = repositories.get(link.getUrl());
            if (existingLink != null) {
               // Kalau sudah ada, cukup tambahkan koneksi (source page + anchor text)
               existingLink.addConnection(currLink, anchorText);
               continue;
            }

            // URL ini belum pernah tercatat maka tambahkan koneksi pertama
            link.addConnection(currLink, anchorText);

            // Tentukan host-nya buat bedain same-host vs external
            String host = UrlHandler.getHost(link.getUrl());

            /**
             * Kalau host sama dengan rootHost maka anggap sebagai webpage dan masukkan ke
             * frontier
             */
            if (host.equalsIgnoreCase(rootHost)) {
               /*
                * Untuk webpage same-host:
                * - Tidak langsung dimasukkan ke repositories di sini.
                * - Link baru akan dianggap dicek dan dihitung ketika
                * nanti diambil dari frontier dan di-fetch di loop BFS utama.
                */
               frontier.offer(link);
            } else {
               /*
                * Untuk link non-same-host (external / beda host):
                * - Dicek secara paralel di virtual thread.
                * - Karena link ini akan benar-benar dicek (fetchLink),
                * maka kita catat ke repositories
                */

               // Pastikan limit belum terlewati sebelum mencatat link baru
               if (repositories.size() >= MAX_LINKS) {
                  frontier.clear();
                  break;
               }

               // Masukkan ke repositories sebagai link baru
               repositories.putIfAbsent(link.getUrl(), link);

               // Submit task ke virtual thread buat cek fetch link
               executor.submit(() -> {
                  /*
                   * Terapkan rate limiting per host biar ga dianggap serangan atau
                   * kena error HTTP 429 (Too Many Requests).
                   */
                  RateLimiter limiter = rateLimiters.computeIfAbsent(host, h -> new RateLimiter());
                  limiter.delay();

                  // Fetch URL tanpa parse HTML (kita cuma butuh status + header)
                  fetchLink(link, false);

                  // Kirim hasil ke controller
                  send(link);
               });
            }
         }

         // Tutup executor: tidak menerima task baru lagi
         executor.shutdown();

         // Tunggu sampai semua virtual thread selesai sebelum lanjut ke halaman frontier
         // berikutnya
         executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }
}
