package com.unpar.brokenlinkchecker.cores;

import com.unpar.brokenlinkchecker.models.Link;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Kelas ini tugasnya buat nyimpen hasil broken link ke file Excel (.xlsx).
 * Jadi setelah crawling selesai, user bisa langsung simpan hasilnya
 * biar ga harus crawling ulang tiap butuh datanya.
 */
public class Exporter {

   /**
    * Method utama yang nge-handle proses export ke Excel.
    *
    * @param brokenLinks daftar link yang ketemu error waktu crawling
    * @param file        file tujuan (.xlsx) tempat data mau disimpan
    * @throws IOException kalau ada masalah waktu nulis file
    */
   public void save(List<Link> brokenLinks, File file) throws IOException {

      // Bikin workbook baru. Pake try-with-resources biar otomatis ke-close.
      try (Workbook workbook = new XSSFWorkbook()) {

         // Bikin satu sheet dengan nama "Broken Links"
         Sheet sheet = workbook.createSheet("Broken Links");

         // ======================== STYLING ========================
         // Style untuk header tabel (bold + background)
         CellStyle headerStyle = createHeaderStyle(workbook);

         // Style buat baris ganjil (warna abu2 muda)
         CellStyle oddRowStyle = createRowStyle(workbook, new Color(245, 245, 245));

         // Style buat baris genap (warna putih)
         CellStyle evenRowStyle = createRowStyle(workbook, Color.WHITE);

         // Style khusus untuk kolom anchor text, biar text bisa wrap
         CellStyle wrapStyle = workbook.createCellStyle();
         wrapStyle.setWrapText(true);

         // ======================== HEADER ========================
         // Daftar judul kolom buat di baris pertama
         String[] headers = {
               "URL",
               "Final URL",
               "Content Type",
               "Error",
               "Source Webpage",
               "Anchor Text"
         };

         // Buat baris header di row index 0
         Row headerRow = sheet.createRow(0);

         // Loop semua nama kolom
         for (int i = 0; i < headers.length; i++) {
            // Bikin cell di kolom i
            Cell cell = headerRow.createCell(i);
            // Isi text header
            cell.setCellValue(headers[i]);
            // Kasih style header
            cell.setCellStyle(headerStyle);
         }

         // ======================== ISI DATA ========================
         // Mulai tulis data dari baris index 1 karena 0 dipakai header
         int rowIndex = 1;

         // Loop semua broken link
         for (Link link : brokenLinks) {

            // Setiap link bisa muncul dari banyak halaman,
            // jadi kita looping juga setiap koneksi/source-nya
            for (Map.Entry<Link, String> entry : link.getConnection().entrySet()) {

               // Buat baris baru
               Row row = sheet.createRow(rowIndex);

               // Tentukan warna baris (zebra striping)
               CellStyle rowStyle = (rowIndex % 2 == 0) ? evenRowStyle : oddRowStyle;

               // Isi data kolom satu per satu
               createStyledCell(row, 0, link.getUrl(), rowStyle);
               createStyledCell(row, 1, link.getFinalUrl(), rowStyle);
               createStyledCell(row, 2, link.getContentType(), rowStyle);
               createStyledCell(row, 3, link.getError(), rowStyle);
               createStyledCell(row, 4, entry.getKey().getUrl(), rowStyle);

               // Kolom terakhir khusus anchor text
               Cell anchorCell = row.createCell(5);
               anchorCell.setCellValue(entry.getValue());
               anchorCell.setCellStyle(wrapStyle);

               // Pindah ke baris berikutnya
               rowIndex++;
            }
         }

         // ======================== 4. AUTO-SIZE ========================
         // Biar kolom otomatis lebar sesuai isinya
         for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
         }

         // ======================== 5. TULIS FILE ========================
         // Tulis workbook ke file tujuan
         try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
         }
      }
   }

   /**
    * Method untuk kasi styling ke header.
    *
    * @param wb workbook yang lagi dipake
    * @return style yang udah siap dipakai buat header
    */
   private CellStyle createHeaderStyle(Workbook wb) {
      CellStyle style = wb.createCellStyle();

      // Font baru khusus header
      Font font = wb.createFont();

      // header selalu bold
      font.setBold(true);

      style.setFont(font);

      // Warna background abu muda
      style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      // Tambah border ke semua sisi
      createBorder(style);

      return style;
   }

   /**
    * Method untuk bikin style biar baris data di tabel (bagian isi) belang-belang.
    *
    * @param wb       workbook yang lagi dipake
    * @param awtColor warna background yang mau dipake buat baris
    * @return style yang udah siap dipake di baris tersebut
    */
   private CellStyle createRowStyle(Workbook wb, Color awtColor) {
      CellStyle style = wb.createCellStyle();

      // Set warna background baris
      style.setFillForegroundColor(new XSSFColor(awtColor, null));
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      // Border biar lebih rapi
      createBorder(style);

      return style;
   }

   /**
    * Method untuk menambahkan styling pada cell
    *
    * @param row   baris tempat cell mau dibuat
    * @param col   kolom ke berapa cell-nya
    * @param value isi cell, kalau null nanti diganti jadi string kosong
    * @param style style yang mau dipasang ke cell ini
    */
   private void createStyledCell(Row row, int col, String value, CellStyle style) {
      Cell cell = row.createCell(col); // bikin cell
      cell.setCellValue(value != null ? value : ""); // antisipasi null
      cell.setCellStyle(style); // pasang style
   }

   /**
    * Nambahin border tipis di semua sisi style.
    *
    * @param style style yang mau ditambahin border
    */
   private void createBorder(CellStyle style) {
      style.setBorderBottom(BorderStyle.THIN); // bawah
      style.setBorderTop(BorderStyle.THIN); // atas
      style.setBorderLeft(BorderStyle.THIN); // kiri
      style.setBorderRight(BorderStyle.THIN); // kanan
   }
}
