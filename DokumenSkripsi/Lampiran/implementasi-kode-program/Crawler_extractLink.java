private Map<Link, String> extractLink(Document html) {
   Map<Link, String> result = new HashMap<>();

   for (Element a : html.select("a[href]")) {
      String absoluteUrl = a.absUrl("href");

      String normalizedUrl = UrlHandler.normalizeUrl(absoluteUrl);

      if (normalizedUrl == null) continue;

      Link link = new Link(normalizedUrl);

      String anchorText = a.text().trim();

      result.putIfAbsent(link, anchorText);
   }
   
   return result;
}