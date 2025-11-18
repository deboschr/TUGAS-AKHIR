private Map<Link, String> extractLink(Document doc) {
   // Map hasil ekstraksi. Key: Link, Value: teks yang ada di dalam tag a
   Map<Link, String> result = new HashMap<>();

   // Loop semua elemen tag a yang punya atribut href
   for (Element a : doc.select("a[href]")) {

      // Ambil URL absolut dari atribut href (Jsoup akan gabungin dengan baseUri)
      String absoluteUrl = a.absUrl("href");

      // Skip kalau kosong, berarti ini bukan URL valid
      if (absoluteUrl.isEmpty()) {
         continue;
      }

      // Normalize URL biar konsisten (hapus fragment, lower-case host, dsb.)
      String normalizedUrl = UrlHandler.normalizeUrl(absoluteUrl);

      // Skip kalau gagal normalisasi
      if (normalizedUrl == null) {
         continue;
      }

      // Bikin objek Link baru berdasarkan URL yang udah bersih
      Link link = new Link(normalizedUrl);

      // Ambil teks yang ada di link
      String anchorText = a.text().trim();

      // Masukin ke map hanya kalau URL itu belum pernah tercatat sebelumnya
      result.putIfAbsent(link, anchorText);
   }

   // Balikin semua link yang berhasil diekstrak
   return result;
}
