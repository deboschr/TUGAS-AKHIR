public void save(List<Link> data, Summary summary, File file) throws IOException {
   List<Link> brokenLinkData = new ArrayList<>(data);
   brokenLinkData.sort(Comparator.comparingInt(a -> a.getConnection().size()));
   try (Workbook workbook = new XSSFWorkbook()) {
      this.headerStyle = createRowStyle(workbook, Color.decode("#2f5d50"), true, true, Color.decode("#f1f0eb"), 16);
      this.oddRowStyle = createRowStyle(workbook, Color.decode("#f4ebdb"), false, false, Color.decode("#222222"),
            12);
      this.evenRowStyle = createRowStyle(workbook, Color.decode("#b6c5bf"), false, false, Color.decode("#222222"),
            12);
      this.otherStyle = createRowStyle(workbook, Color.decode("#efefef"), true, true, Color.decode("#222222"), 12);
      this.emptyStyle = workbook.createCellStyle();

      Sheet summarySheet = workbook.createSheet("Summary");
      writeProcessSummaryTable(summarySheet, summary);
      writeBrokenLinkSummaryTable(summarySheet, brokenLinkData);
      Sheet brokenLinkSheet = workbook.createSheet("Broken Links");
      writeBrokenLinkTable(brokenLinkSheet, brokenLinkData);

      try (FileOutputStream fos = new FileOutputStream(file)) {
         workbook.write(fos);
      }
   }
}