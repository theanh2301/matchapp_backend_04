package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.service.excel.ExcelUserProcessService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ExcelUserService {

    private final UserRepository userRepository;
    private final ExcelUserProcessService excelUserProcessService;


    /*public ImportResult importExcel(MultipartFile file) {
        try {
            InputStream is = file.getInputStream();
            return excelUserProcessService.processImport(is);
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }*/

    public ByteArrayInputStream exportExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Users");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Full Name");
            header.createCell(1).setCellValue("DOB (yyyy-MM-dd)");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Email");
            header.createCell(4).setCellValue("Phone");
            header.createCell(5).setCellValue("Password");
            header.createCell(6).setCellValue("Avatar URL");
            header.createCell(7).setCellValue("Is Premium");
            header.createCell(8).setCellValue("Role");
            header.createCell(9).setCellValue("Grade ID");

            int rowIdx = 1;

            for (User u : userRepository.findAll()) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(u.getFullName());
                row.createCell(1).setCellValue(u.getDob() != null ? u.getDob().toString() : "");
                row.createCell(2).setCellValue(u.getStatus() != null ? u.getStatus() : "");
                row.createCell(3).setCellValue(u.getEmail());
                row.createCell(4).setCellValue(u.getPhone() != null ? u.getPhone() : "");
                row.createCell(5).setCellValue("");
                row.createCell(6).setCellValue(u.getAvatarUrl() != null ? u.getAvatarUrl() : "");
                row.createCell(7).setCellValue(u.getIsPremium() != null && u.getIsPremium());
                row.createCell(8).setCellValue(u.getRole().name());
                row.createCell(9).setCellValue(u.getGrade().getId());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage());
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

    private Boolean getBoolean(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getBooleanCellValue();
        } catch (Exception e) {
            String val = getString(cell);
            return val != null && (val.equalsIgnoreCase("true") || val.equals("1"));
        }
    }
}