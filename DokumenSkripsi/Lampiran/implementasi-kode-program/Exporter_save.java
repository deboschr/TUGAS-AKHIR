public void save(List<Link> brokenLinks, File file) throws IOException {
   try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Broken Links");

      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle oddRowStyle = createRowStyle(workbook, new Color(245, 245, 245));
      CellStyle evenRowStyle = createRowStyle(workbook, Color.WHITE);
      CellStyle wrapStyle = workbook.createCellStyle();
      wrapStyle.setWrapText(true);

      String[] headers = {"URL", "Final URL", "Content Type", "Error", "Source Webpage", "Anchor Text"};
      Row headerRow = sheet.createRow(0);

      for (int i = 0; i < headers.length; i++) {
         Cell cell = headerRow.createCell(i);
         cell.setCellValue(headers[i]);
         cell.setCellStyle(headerStyle);
      }

      int rowIndex = 1;
      for (Link link : brokenLinks) {
         for (Map.Entry<Link, String> entry : link.getConnection().entrySet()) {
            Row row = sheet.createRow(rowIndex);
            CellStyle rowStyle = (rowIndex % 2 == 0) ? evenRowStyle : oddRowStyle;

            createStyledCell(row, 0, link.getUrl(), rowStyle);
            createStyledCell(row, 1, link.getFinalUrl(), rowStyle);
            createStyledCell(row, 2, link.getContentType(), rowStyle);
            createStyledCell(row, 3, link.getError(), rowStyle);
            createStyledCell(row, 4, entry.getKey().getUrl(), rowStyle);

            Cell anchorCell = row.createCell(5);
            anchorCell.setCellValue(entry.getValue());
            anchorCell.setCellStyle(wrapStyle);

            rowIndex++;
         }
      }

      for (int i = 0; i < headers.length; i++) {
         sheet.autoSizeColumn(i);
      }

      try (FileOutputStream fos = new FileOutputStream(file)) {
         workbook.write(fos);
      }
   }
}
