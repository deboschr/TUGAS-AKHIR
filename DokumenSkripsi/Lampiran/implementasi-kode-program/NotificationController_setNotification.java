public void setNotification(String type, String message) {
   messageLabel.setText(message);
   type = type.toUpperCase();
   switch (type) {
      case "ERROR" -> applyStyle("#dc2626", "\u2716", "ERROR");
      case "WARNING" -> applyStyle("#f59e0b", "\u26A0", "WARNING");
      case "SUCCESS" -> applyStyle("#10b981", "\u2714", "SUCCESS");
      default -> applyStyle("#6b7280", "\u2753", "UNKNOWN");
   }
}