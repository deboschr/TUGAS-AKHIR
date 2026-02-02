public static String normalizeUrl(String rawUrl, boolean isStrict) {
   if (rawUrl == null || rawUrl.trim().isEmpty()) {
      return null
   }

   try {
      URI uri = new URI(rawUrl.trim());

      String scheme = uri.getScheme();
      String host = uri.getHost();
      int port = uri.getPort();
      String path = uri.getRawPath();
      String query = uri.getRawQuery();

      if (isStrict) {
         if (scheme == null || scheme.isEmpty()) {
            return null
         };
         if (host == null || host.isEmpty()) {
            return null
         };
      }

      if (!scheme.equalsIgnoreCase("http") && 
         !scheme.equalsIgnoreCase("https")) {
         return null
      }

      if ((scheme.equalsIgnoreCase("http") && port == 80) || 
         (scheme.equalsIgnoreCase("https") && port == 443)) {
         port = -1
      }

      path = normalizePath(path);

      URI cleaned = new URI(scheme.toLowerCase(), null, host.toLowerCase(), port, path, query, null);

      return cleaned.toASCIIString();
   } catch (Exception e) {
      return rawUrl;
   }
}