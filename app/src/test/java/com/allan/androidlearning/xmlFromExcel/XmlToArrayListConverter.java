package com.allan.androidlearning.xmlFromExcel;

import com.allan.androidlearning.common.StringImportBean;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlToArrayListConverter {
    private final String xmlFile;

    public XmlToArrayListConverter(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public List<StringImportBean> load() {
        try (FileInputStream file = new FileInputStream(xmlFile);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<StringImportBean> resultList = new ArrayList<>();

            var rowNum = sheet.getLastRowNum();
            System.out.println(xmlFile + " Total rows: " + rowNum);
            for (int i = 1; i <= rowNum; i++) {
                Row currentRow = sheet.getRow(i);
                if (currentRow == null) continue;

                StringImportBean item = new StringImportBean();
                item.key = getCellValueAsString(currentRow.getCell(0));
                item.translatable = getCellValueAsString(currentRow.getCell(1));
                item.en = getCellValueAsString(currentRow.getCell(3));
                item.enAdj = getCellValueAsString(currentRow.getCell(4));
                item.de = getCellValueAsString(currentRow.getCell(5));
                item.deAdj = getCellValueAsString(currentRow.getCell(6));
                item.fr = getCellValueAsString(currentRow.getCell(7));
                item.frAdj = getCellValueAsString(currentRow.getCell(8));
                item.valueMarker = getCellValueAsString(currentRow.getCell(9));

                resultList.add(item);
            }

            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double num = cell.getNumericCellValue();
                    return num == (int) num ? String.valueOf((int) num) : String.valueOf(num);
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }
}