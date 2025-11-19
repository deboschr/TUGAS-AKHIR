private void setTableView() {
   brokenLinkTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
   brokenLinkTable.setItems(brokenLinks);

   errorColumn.setCellValueFactory(cell -> cell.getValue().errorProperty());
   urlColumn.setCellValueFactory(cell -> cell.getValue().urlProperty());

   brokenLinkTable.setRowFactory(tv -> {
      TableRow<Link> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
         if (!row.isEmpty() && event.getClickCount() == 1) {
            Link clickedLink = row.getItem();
            Application.openLinkWindow(clickedLink);
         }
      });
      return row;
   });

   errorColumn.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String status, boolean empty) {
         super.updateItem(status, empty);
         if (empty || status == null) {
            setText(null);
            setStyle("");
         } else {
            setText(status);

            Link link = getTableView().getItems().get(getIndex());
            int code = link.getStatusCode();
            
            if (code >= 400 && code < 600) {
               setStyle("-fx-text-fill: -grey-dark; -fx-font-weight: bold;");
            } else {
               setStyle("-fx-text-fill: -red; -fx-font-weight: bold;");
            }
         }
      }
   });

   urlColumn.setCellFactory(col -> new TableCell<>() {
      private final Hyperlink link = new Hyperlink();

      {
         link.setOnAction(e -> {
            String url = link.getText();
            try {
               if (Desktop.isDesktopSupported()) {
                  Desktop.getDesktop().browse(new URI(url));
               }
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         });
      }

      @Override
      protected void updateItem(String item, boolean empty) {
         super.updateItem(item, empty);
         if (empty || item == null) {
            setGraphic(null);
         } else {
            link.setText(item);
            setGraphic(link);
         }
      }
   });
}