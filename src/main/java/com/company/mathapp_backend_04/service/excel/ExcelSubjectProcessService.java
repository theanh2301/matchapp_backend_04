package com.company.mathapp_backend_04.service.excel;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelSubjectProcessService {

    private static final String ERROR_FILE_DIR = "uploads/";
    private static final int BATCH_SIZE = 100;

    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;

    @Async
    @Transactional
    public CompletableFuture<ImportResult> processImportAsync(InputStream fileInputStream) {
        int successCount = 0;
        int errorCount = 0;
        int totalCount = 0;

        List<String> header = new ArrayList<>();
        List<List<String>> errorData = new ArrayList<>();
        List<Subject> pendingSubjects = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (int index = 0; index < headerRow.getLastCellNum(); index++) {
                    header.add(getString(headerRow.getCell(index)));
                }
                header.add("Error Message");
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isBlankRow(row, 3)) {
                    continue;
                }

                totalCount++;

                List<String> rowData = readRow(row, 3);
                try {
                    String subjectName = getString(row.getCell(0));
                    String icon = getString(row.getCell(1));
                    Double gradeIdRaw = getNumeric(row.getCell(2));

                    if (subjectName == null || subjectName.isBlank()) {
                        throw new RuntimeException("Subject name is required");
                    }
                    if (gradeIdRaw == null) {
                        throw new RuntimeException("Grade ID is required");
                    }

                    String normalizedSubjectName = subjectName.trim();
                    int gradeId = gradeIdRaw.intValue();
                    String importKey = normalizedSubjectName.toLowerCase() + "|" + gradeId;

                    if (!processedKeys.add(importKey)) {
                        throw new RuntimeException("Duplicate subject in import file for the same grade");
                    }

                    Grade grade = gradeRepository.findById(gradeId)
                            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeId));

                    Subject subject = subjectRepository.findBySubjectNameIgnoreCaseAndGrade_Id(
                                    normalizedSubjectName,
                                    gradeId
                            )
                            .orElseGet(Subject::new);

                    subject.setSubjectName(normalizedSubjectName);
                    subject.setIcon(icon == null || icon.isBlank() ? null : icon.trim());
                    subject.setGrade(grade);
                    pendingSubjects.add(subject);
                    successCount++;

                    if (pendingSubjects.size() >= BATCH_SIZE) {
                        subjectRepository.saveAll(pendingSubjects);
                        pendingSubjects.clear();
                    }
                } catch (Exception exception) {
                    rowData.add(exception.getMessage());
                    errorData.add(rowData);
                    errorCount++;
                }
            }

            if (!pendingSubjects.isEmpty()) {
                subjectRepository.saveAll(pendingSubjects);
            }

            String errorFilePath = null;
            if (!errorData.isEmpty()) {
                errorFilePath = createErrorExcel(header, errorData);
            }

            ImportResult result = new ImportResult(totalCount, successCount, errorCount, errorFilePath);
            log.info("Subject import done: success={}, error={}, total={}", successCount, errorCount, totalCount);
            return CompletableFuture.completedFuture(result);
        } catch (Exception exception) {
            throw new RuntimeException("Error processing subject import: " + exception.getMessage(), exception);
        }
    }

    private List<String> readRow(Row row, int columnCount) {
        List<String> rowData = new ArrayList<>();
        for (int index = 0; index < columnCount; index++) {
            rowData.add(getString(row.getCell(index)));
        }
        return rowData;
    }

    private boolean isBlankRow(Row row, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            String value = getString(row.getCell(index));
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String createErrorExcel(List<String> header, List<List<String>> data) throws IOException {
        File directory = new File(ERROR_FILE_DIR);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create directory: " + ERROR_FILE_DIR);
        }

        String filePath = ERROR_FILE_DIR + "error_subjects_" + System.currentTimeMillis() + ".xlsx";
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            Sheet sheet = workbook.createSheet("Errors");
            int rowIndex = 0;

            Row headerRow = sheet.createRow(rowIndex++);
            for (int index = 0; index < header.size(); index++) {
                headerRow.createCell(index).setCellValue(header.get(index));
            }

            for (List<String> rowData : data) {
                Row dataRow = sheet.createRow(rowIndex++);
                for (int index = 0; index < rowData.size(); index++) {
                    dataRow.createCell(index).setCellValue(rowData.get(index));
                }
            }

            workbook.write(fileOutputStream);
        }

        return filePath;
    }

    private String getString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private Double getNumeric(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            return cell.getNumericCellValue();
        } catch (Exception exception) {
            String value = getString(cell);
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
