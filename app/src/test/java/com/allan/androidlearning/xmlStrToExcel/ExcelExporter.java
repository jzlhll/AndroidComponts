package com.allan.androidlearning.xmlStrToExcel;

import com.allan.androidlearning.common.StringExportBean;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.HashMap;

public class ExcelExporter {
    String[] headers = {"Key", "Translatable", "Module", "EN", "DE", "FR"};

    Workbook workbook;
    Sheet sheet;
    int rowNum = 1;

    public void startXml() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("String Resources");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    private static String removeYinHao(String key, String string) {
        if (string.isEmpty()) {
            return string;
        }
        try {
            var firstChar = string.charAt(0);
            var lastChar = string.charAt(string.length() - 1);
            if (firstChar == '\"' && lastChar == '\"') {
                return string.substring(1, string.length() - 1);
            }
            return string;
        } catch (Exception e) {
            System.out.println(key + " Error: string " + string);
            throw e;
        }

    }

    public void exportToExcel(HashMap<String, StringExportBean> resultMap) {
        // 填充数据
        for (String key : resultMap.keySet()) {
            StringExportBean bean = resultMap.get(key);
            Row row = sheet.createRow(rowNum++);

            int n = 0;
            row.createCell(n++).setCellValue(key);
            row.createCell(n++).setCellValue(bean.translatable);
            row.createCell(n++).setCellValue(bean.moduleName);
            var en = bean.en != null ? bean.en : "";
            row.createCell(n++).setCellValue(removeYinHao(key, en));
            var de = bean.de != null ? bean.de : "";
            row.createCell(n++).setCellValue(removeYinHao(key, de));
            var fr = bean.fr != null ? bean.fr : "";
            row.createCell(n).setCellValue(removeYinHao(key, fr));
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void endXml(String outputPath) throws Exception {
        // 写入文件
        try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
            workbook.write(outputStream);
        }

        workbook.close();
    }
}
