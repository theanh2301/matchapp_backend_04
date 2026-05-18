package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.service.GradeService;
import com.company.mathapp_backend_04.service.admin.AdminUserService;
import com.company.mathapp_backend_04.service.admin.ExcelUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;
    private final ExcelUserService excelUserService;
    private final GradeService gradeService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "30") int size,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage = userService.getAll(keyword.trim(), pageable);

        model.addAttribute("grades", gradeService.getAll());
        model.addAttribute("keyword", keyword.trim());
        model.addAttribute("users", userPage);
        model.addAttribute("activeMenu", "users");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/user-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute User user, @RequestParam Integer gradeId) {
        try {
            if (user.getId() == null) {
                userService.create(user, gradeId);
            } else {
                userService.update(user, gradeId);
            }
            return "redirect:/admin/users";
        } catch (Exception e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Integer> ids) {
        userService.deleteBulk(ids);
        return "redirect:/admin/users";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            ImportResult result = excelUserService.importExcel(file);
            String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
            if (result.getError() > 0) {
                return "redirect:/admin/users?message=" + encode(message)
                        + "&error=" + encode("Failed rows: " + result.getError()
                        + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
            }
            return "redirect:/admin/users?message=" + encode(message);
        } catch (Exception exception) {
            return "redirect:/admin/users?error=" + encode(exception.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream in = excelUserService.exportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .body(new InputStreamResource(in));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
