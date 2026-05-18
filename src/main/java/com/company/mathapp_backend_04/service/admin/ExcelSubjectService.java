package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.model.response.ImportResult;
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

    public ImportResult importExcel(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Please select an Excel file");
            }
            InputStream inputStream = file.getInputStream();
            return excelSubjectProcessService.processImportAsync(inputStream).join();
        } catch (Exception exception) {
            throw new RuntimeException("Cannot import subject: " + exception.getMessage(), exception);
        }
    }

    public ByteArrayInputStream exportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Subjects");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Subject Name");
            header.createCell(1).setCellValue("Icon");
            header.createCell(2).setCellValue("Grade ID");

            int rowIndex = 1;
            for (Subject subject : subjectRepository.findAll()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(subject.getSubjectName());
                row.createCell(1).setCellValue(subject.getIcon() != null ? subject.getIcon() : "");
                row.createCell(2).setCellValue(subject.getGrade() != null ? subject.getGrade().getId() : 0);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Export subject failed: " + exception.getMessage(), exception);
        }
    }
}
