public static void openMainWindow() {
   try {
      FXMLLoader loader = new FXMLLoader(MAIN_FXML);

      Scene scene = new Scene(loader.load());

      MAIN_STAGE.setScene(scene);
      MAIN_STAGE.initStyle(StageStyle.UNDECORATED);
      MAIN_STAGE.centerOnScreen();
      MAIN_STAGE.setMaximized(true);
      MAIN_STAGE.setMinWidth(1024);
      MAIN_STAGE.setMinHeight(600);
      MAIN_STAGE.show();
   } catch (Exception e) {
      e.printStackTrace();
   }
}
