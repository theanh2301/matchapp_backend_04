package com.company.mathapp_backend_04.service.excel;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Role;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.io.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelUserProcessService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final UserStatService userStatService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String ERROR_FILE_DIR = "uploads/";

    @Async
    @Transactional
    public CompletableFuture<ImportResult> processImport(InputStream fileInputStream) {

        int successCount = 0;
        int errorCount = 0;
        int totalCount = 0;

        List<List<String>> errorData = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<String> header = new ArrayList<>();
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    header.add(getString(headerRow.getCell(i)));
                }
                header.add("Error Message");
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                totalCount++;

                List<String> rowData = new ArrayList<>();
                String errorMessage = "";

                try {
                    String fullName = getString(row.getCell(0));
                    String dobStr = getString(row.getCell(1));
                    String status = getString(row.getCell(2));
                    String email = getString(row.getCell(3));
                    String phone = getString(row.getCell(4));
                    String password = getString(row.getCell(5));
                    String avatarUrl = getString(row.getCell(6));
                    Boolean isPremium = getBoolean(row.getCell(7));
                    String roleStr = getString(row.getCell(8));
                    Double gradeIdRaw = getNumeric(row.getCell(9));

                    for (int i = 0; i < 10; i++) {
                        rowData.add(getString(row.getCell(i)));
                    }

                    // validate
                    if (email == null || email.isEmpty()) throw new RuntimeException("Email is required");
                    if (fullName == null || fullName.isEmpty()) throw new RuntimeException("FullName is required");
                    if (password == null || password.isEmpty()) throw new RuntimeException("Password is required");
                    if (gradeIdRaw == null) throw new RuntimeException("GradeId is required");

                    if (userRepository.findByEmail(email).isPresent()) {
                        throw new RuntimeException("Email already exists");
                    }

                    Grade grade = gradeRepository.findById(gradeIdRaw.intValue())
                            .orElseThrow(() -> new RuntimeException("Grade not found"));

                    User user = new User();
                    user.setFullName(fullName);

                    if (dobStr != null && !dobStr.isEmpty()) {
                        try {
                            user.setDob(LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception e) {
                            throw new RuntimeException("Invalid DOB format (yyyy-MM-dd)");
                        }
                    }

                    user.setStatus(status);
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setAvatarUrl(avatarUrl);
                    user.setIsPremium(isPremium != null ? isPremium : false);

                    user.setRole(
                            (roleStr != null && !roleStr.isEmpty())
                                    ? Role.valueOf(roleStr)
                                    : Role.USER
                    );

                    user.setGrade(grade);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());

                    userRepository.save(user);
                    userStatService.createUserStat(user);

                    successCount++;

                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    errorCount++;
                }

                if (!errorMessage.isEmpty()) {
                    rowData.add(errorMessage);
                    errorData.add(rowData);
                }
            }

            String errorFilePath = null;
            if (!errorData.isEmpty()) {
                errorFilePath = createErrorExcel(header, errorData);
            }

            log.info("Import done: success={}, error={}, total={}",
                    successCount, errorCount, totalCount);

            ImportResult result = new ImportResult(totalCount, successCount, errorCount, errorFilePath);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel: " + e.getMessage(), e);
        }
    }


    // ================== TẠO FILE EXCEL LỖI ==================
    private String createErrorExcel(List<String> header, List<List<String>> data) throws IOException {

        File dir = new File(ERROR_FILE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Cannot create directory: " + ERROR_FILE_DIR);
            }
        }

        String filePath = ERROR_FILE_DIR + "error_users_" + System.currentTimeMillis() + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Errors");

            int rowIndex = 0;

            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < header.size(); i++) {
                headerRow.createCell(i).setCellValue(header.get(i));
            }

            for (List<String> rowData : data) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < rowData.size(); i++) {
                    row.createCell(i).setCellValue(rowData.get(i));
                }
            }

            for (int i = 0; i < header.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
        }

        return filePath;
    }

    // ================== HELPER ==================
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