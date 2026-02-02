public void setNotifValue() {
   messageLabel.setText(message);
   
   switch (type) {
      case "ERROR" -> applyStyle("-red", "\u2716", "ERROR");
      case "WARNING" -> applyStyle("-orange", "\u26A0", "WARNING");
      case "SUCCESS" -> applyStyle("-green", "\u2714", "SUCCESS");
      default -> applyStyle("-grey-light", "\u2753", "UNKNOWN");
   }
}