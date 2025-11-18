public void setLink(Link link) {
   urlField.setText(link.getUrl());
   finalUrlField.setText(link.getFinalUrl());
   contentTypeField.setText(link.getContentType());
   errorField.setText(link.getError());
   webpageLinks.setAll(link.getConnection().entrySet());

   makeFieldClickable(urlField);
   makeFieldClickable(finalUrlField);
   
   setTableView();
}