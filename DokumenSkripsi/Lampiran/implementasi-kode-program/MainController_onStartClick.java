private void onStartClick() {
   try {
      String seedUrl = seedUrlField.getText().trim();
      String cleanedSeedUrl = URLHandler.normalizeUrl(seedUrl);

      if (cleanedSeedUrl == null) {
         showNofication("WARNING", "Please enter a valid URL before starting.");
         return;
      }

      seedUrlField.setText(cleanedSeedUrl);
      summary.setStatus(Status.CHECKING);
      allLinks.clear();
      
      Thread.startVirtualThread(() -> {
         try {
            summary.setStartTime(System.currentTimeMillis());
            crawler.start(cleanedSeedUrl);
            summary.setEndTime(System.currentTimeMillis());
            
            if (!crawler.isStopped()) {
               Platform.runLater(() -> summary.setStatus(Status.COMPLETED));
            }
         } catch (Exception e) {
            showNofication("ERROR", e.getMessage());
         }
      });
   } catch (Exception e) {
      showNofication("ERROR", e.getMessage());
   }
}