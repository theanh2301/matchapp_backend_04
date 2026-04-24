package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.service.GradeService;
import com.company.mathapp_backend_04.service.admin.AdminUserService;
import com.company.mathapp_backend_04.service.admin.ExcelUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayInputStream;

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
                       @RequestParam(defaultValue = "30") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        String safeKeyword = keyword.trim();

        Page<User> userPage;
        if (safeKeyword.isEmpty()) {
            userPage = userService.getAll(null, pageable);
        } else {
            userPage = userService.getAll(safeKeyword, pageable);
        }

        model.addAttribute("grades", gradeService.getAll());
        model.addAttribute("keyword", safeKeyword);
        model.addAttribute("users", userPage);
        model.addAttribute("activeMenu", "users");

        return "pages/user-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute User user,
                       @RequestParam Integer gradeId,
                       Model model) {
        try {
            if (user.getId() == null) {
                userService.create(user, gradeId);
            } else {
                userService.update(user, gradeId);
            }
            return "redirect:/admin/users";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("grades", gradeService.getAll());
            model.addAttribute("users", userService.getAll(null,
                    PageRequest.of(0, 30)));

            return "pages/user-list"; // quay lại modal page
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    /*@PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {

        ImportResult result = excelUserService.importExcel(file);

        model.addAttribute("message",
                "Import thành công " + result.getSuccess() + "/" + result.getTotal());

        model.addAttribute("errorFile", result.getErrorFilePath());

        return "redirect:/admin/users";
    }*/

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {

        ByteArrayInputStream in = excelUserService.exportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .body(new InputStreamResource(in));
    }
}