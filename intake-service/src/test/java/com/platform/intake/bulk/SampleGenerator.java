package com.platform.intake.bulk;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.UUID;
import java.time.Instant;

public class SampleGenerator {
    public static void main(String[] args) throws Exception {
        String[] headers = { "eventid", "producerid", "eventtype", "correlationid", "timestamp", "payload" };

        // Generate CSV
        try (FileWriter writer = new FileWriter("sample_bulk.csv")) {
            writer.write(String.join(",", headers) + "\n");
            for (int i = 1; i <= 50; i++) {
                writer.write(String.format(
                        "%s,producer-%d,%s,%s,%s,\"{\\\"orderId\\\":\\\"ORD-%d\\\",\\\"amount\\\":%.2f}\"\n",
                        UUID.randomUUID(), (i % 5 + 1),
                        (i % 2 == 0 ? "ORDER_CREATED" : "USER_SIGNED_UP"),
                        UUID.randomUUID(), Instant.now(), 1000 + i, 99.99 + i));
            }
        }
        System.out.println("Generated sample_bulk.csv");

        // Generate XLSX
        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream fileOut = new FileOutputStream("sample_bulk.xlsx")) {
            Sheet sheet = workbook.createSheet("Events");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 1; i <= 50; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(UUID.randomUUID().toString());
                row.createCell(1).setCellValue("producer-" + (i % 5 + 1));
                row.createCell(2).setCellValue(i % 2 == 0 ? "ORDER_CREATED" : "USER_SIGNED_UP");
                row.createCell(3).setCellValue(UUID.randomUUID().toString());
                row.createCell(4).setCellValue(Instant.now().toString());
                row.createCell(5)
                        .setCellValue(String.format("{\"orderId\":\"ORD-%d\",\"amount\":%.2f}", 1000 + i, 99.99 + i));
            }
            workbook.write(fileOut);
        }
        System.out.println("Generated sample_bulk.xlsx");
    }
}
