package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import com.company.mathapp_backend_04.service.excel.ExcelSubjectProcessService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ExcelSubjectService {

    private final SubjectRepository subjectRepository;
    private final ExcelSubjectProcessService excelSubjectProcessService;

    /**
     * IMPORT EXCEL (chạy async)
     */
    public void importExcel(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            excelSubjectProcessService.processImportAsync(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Không thể import subject: " + e.getMessage(), e);
        }
    }

    /**
     * EXPORT EXCEL
     */
    public ByteArrayInputStream exportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Subjects");

            // HEADER
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Subject Name");
            header.createCell(1).setCellValue("Icon");
            header.createCell(2).setCellValue("Grade ID");

            int rowIdx = 1;

            for (Subject s : subjectRepository.findAll()) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(s.getSubjectName());
                row.createCell(1).setCellValue(s.getIcon() != null ? s.getIcon() : "");
                row.createCell(2).setCellValue(
                        s.getGrade() != null ? s.getGrade().getId() : 0
                );
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Export Subject failed: " + e.getMessage());
        }
    }
}