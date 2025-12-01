public void save(File file) throws IOException {
   try (Workbook workbook = new XSSFWorkbook()) {

      this.headerStyle = createRowStyle(workbook, Color.decode("#2f5d50"), true, true, Color.decode("#f1f0eb"), 16);
      this.oddRowStyle = createRowStyle(workbook, Color.decode("#f4ebdb"), false, false, Color.decode("#222222"), 12);
      this.evenRowStyle = createRowStyle(workbook, Color.decode("#b6c5bf"), false, false, Color.decode("#222222"), 12);
      this.otherStyle = createRowStyle(workbook, Color.decode("#efefef"), true, true, Color.decode("#222222"), 12);
      this.emptyStyle = workbook.createCellStyle();

      Sheet summarySheet = workbook.createSheet("Summary");
      writeResultSummaryTable(summarySheet);
      writeBrokenLinkSummaryTable(summarySheet);

      Sheet brokenLinkSheet = workbook.createSheet("Broken Links");
      writeBrokenLinkTable(brokenLinkSheet);

      try (FileOutputStream fos = new FileOutputStream(file)) {
         workbook.write(fos);
      }
   }
}