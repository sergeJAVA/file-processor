package com.example.file_processor.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;


@Component
public class ExcelFileValidator implements FileValidator{

    @Override
    public boolean validate(byte[] fileBytes) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    return false;
                }

                for (int colIndex = 0; colIndex < 3; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
