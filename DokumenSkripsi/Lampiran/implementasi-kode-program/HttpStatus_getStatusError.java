public static String getStatusError(int statusCode) {
   if (statusCode >= 100 && statusCode < 400) return null

   return STATUS_MAP.getOrDefault(statusCode, String.valueOf(statusCode));
}