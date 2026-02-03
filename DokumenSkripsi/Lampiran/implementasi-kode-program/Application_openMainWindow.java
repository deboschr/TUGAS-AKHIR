public static void openMainWindow() {
   try {
      URL fxml = Application.class.getResource("/com/unpar/brokenlinkscanner/scenes/main-scene.fxml");
      
      FXMLLoader loader = new FXMLLoader(fxml);
      
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