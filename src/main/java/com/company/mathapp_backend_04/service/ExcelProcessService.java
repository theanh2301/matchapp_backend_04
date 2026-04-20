package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Role;
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

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j // Dùng để ghi log tiến trình
public class ExcelProcessService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final UserStatService userStatService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Async // ĐÁNH DẤU HÀM NÀY CHẠY TRONG LUỒNG RIÊNG
    @Transactional
    public void processImportAsync(InputStream fileInputStream) {
        log.info("Bắt đầu xử lý import Excel chạy ngầm...");
        try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) {
                rows.next(); 
            }

            int successCount = 0;
            int errorCount = 0;

            while (rows.hasNext()) {
                Row row = rows.next();
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

                    if (email == null || email.isEmpty() || fullName == null || password == null || gradeIdRaw == null) {
                        errorCount++;
                        continue;
                    }

                    if (userRepository.findByEmail(email).isPresent()) {
                        errorCount++;
                        continue;
                    }

                    Grade grade = gradeRepository.findById(gradeIdRaw.intValue())
                            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeIdRaw));

                    User user = new User();
                    user.setFullName(fullName);
                    
                    if (dobStr != null && !dobStr.isEmpty()) {
                        try {
                            user.setDob(LocalDate.parse(dobStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception ignored) {
                        }
                    }
                    
                    if (status != null && !status.isEmpty()) user.setStatus(status);
                    user.setEmail(email);
                    if (phone != null && !phone.isEmpty()) user.setPhone(phone);
                    user.setPassword(bCryptPasswordEncoder.encode(password));
                    if (avatarUrl != null && !avatarUrl.isEmpty()) user.setAvatarUrl(avatarUrl);
                    user.setIsPremium(isPremium != null ? isPremium : false);
                    user.setRole(roleStr != null && !roleStr.isEmpty() ? Role.valueOf(roleStr) : Role.USER);
                    user.setGrade(grade);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());

                    userStatService.createUserStat(user);
                    userRepository.saveAndFlush(user);
                    successCount++;

                } catch (Exception e) {
                    log.error("Lỗi dòng khi import: {}", e.getMessage());
                    errorCount++;
                }
            }

            log.info("Hoàn tất import Excel. Thành công: {}, Thất bại/Bỏ qua: {}", successCount, errorCount);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý file Excel chạy ngầm", e);
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