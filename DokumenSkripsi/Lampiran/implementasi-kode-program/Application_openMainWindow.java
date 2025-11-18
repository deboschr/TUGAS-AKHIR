public static void openMainWindow() {
   try {
      FXMLLoader loader = new FXMLLoader(
            Application.class.getResource("/com/unpar/brokenlinkchecker/windows/main-window.fxml"));

      Scene scene = new Scene(loader.load());

      mainStage.setScene(scene);
      mainStage.initStyle(StageStyle.UNDECORATED);
      mainStage.centerOnScreen();
      mainStage.setMaximized(true);
      mainStage.setMinWidth(1024);
      mainStage.setMinHeight(600);
      mainStage.show();
   } catch (Exception e) {
      e.printStackTrace();
   }
}
