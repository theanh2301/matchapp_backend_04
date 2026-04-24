package com.company.mathapp_backend_04.service.excel;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelSubjectProcessService {

    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;

    @Async
    @Transactional
    public void processImportAsync(InputStream fileInputStream) {

        log.info("Bắt đầu import SUBJECT...");

        try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Bỏ header
            if (rows.hasNext()) rows.next();

            int success = 0;
            int error = 0;

            while (rows.hasNext()) {
                Row row = rows.next();

                try {
                    String subjectName = getString(row.getCell(0));
                    String icon = getString(row.getCell(1));
                    Double gradeIdRaw = getNumeric(row.getCell(2));

                    // VALIDATE BẮT BUỘC
                    if (subjectName == null || subjectName.isEmpty() || gradeIdRaw == null) {
                        error++;
                        continue;
                    }

                    Grade grade = gradeRepository.findById(gradeIdRaw.intValue())
                            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeIdRaw));

                    // CHECK TRÙNG (optional)
                    boolean exists = subjectRepository
                            .findBySubjectNameContainingIgnoreCase(subjectName)
                            .stream()
                            .anyMatch(s -> s.getSubjectName().equalsIgnoreCase(subjectName)
                                    && s.getGrade().getId().equals(grade.getId()));

                    if (exists) {
                        error++;
                        continue;
                    }

                    Subject subject = new Subject();
                    subject.setSubjectName(subjectName);
                    subject.setIcon(icon);
                    subject.setGrade(grade);

                    subjectRepository.save(subject);
                    success++;

                } catch (Exception e) {
                    log.error("Lỗi dòng subject: {}", e.getMessage());
                    error++;
                }
            }

            log.info("Import SUBJECT xong. Success: {}, Error: {}", success, error);

        } catch (Exception e) {
            log.error("Lỗi đọc file SUBJECT", e);
        }
    }

    private String getString(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Double getNumeric(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }
}