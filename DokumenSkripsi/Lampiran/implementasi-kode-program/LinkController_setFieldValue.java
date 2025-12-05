public void setFieldValue() {
   urlField.setText(link.getUrl());

   finalUrlField.setText(link.getFinalUrl());

   contentTypeField.setText(link.getContentType());

   errorField.setText(link.getError());

   makeFieldClickable(urlField);

   makeFieldClickable(finalUrlField);
}