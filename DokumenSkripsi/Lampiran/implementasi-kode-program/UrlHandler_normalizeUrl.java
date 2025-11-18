public static String normalizeUrl(String rawUrl) {
   if (rawUrl == null || rawUrl.trim().isEmpty()) return null;

   try {
      URI uri = new URI(rawUrl.trim());

      String scheme = uri.getScheme();
      String host = uri.getHost();
      int port = uri.getPort();
      String path = uri.getRawPath();
      String query = uri.getRawQuery();

      if (scheme == null || scheme.isEmpty()) return null;

      if (!scheme.equals("http") && !scheme.equals("https")) return null;

      if (host == null || host.isEmpty()) return null;

      if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) port = -1;

      path = normalizePath(path);

      URI cleaned = new URI(scheme.toLowerCase(), null, host.toLowerCase(), port, path, query, null);

      return cleaned.toASCIIString();
   } catch (Exception e) {
      return rawUrl;
   }
}