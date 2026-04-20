package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.User;
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
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) { // Giới hạn 10 record 1 trang

        // Sắp xếp theo ID giảm dần (mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<User> userPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            userPage = userService.getAll(null, pageable);
        } else {
            userPage = userService.getAll(keyword.trim(), pageable);
        }

        // Truyền đối tượng Page ra View. Lúc này 'users' không còn là ArrayList nữa mà là PageImpl
        model.addAttribute("users", userPage);
        model.addAttribute("keyword", keyword);

        return "users/user-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("grades", gradeService.getAll());
        return "users/user-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute User user,
                         @RequestParam("gradeId") Integer gradeId,
                         Model model) {
        try {
            userService.create(user, gradeId);
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // PHẢI LOAD LẠI GRADES KHI CÓ LỖI
            model.addAttribute("grades", gradeService.getAll());
            return "users/user-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        model.addAttribute("user", userService.getById(id));
        model.addAttribute("grades", gradeService.getAll());
        return "users/user-form";
    }

    @PostMapping("/edit")
    public String update(@ModelAttribute User user,
                         @RequestParam(value = "gradeId", required = false) Integer gradeId,
                         Model model) {
        try {
            userService.update(user, gradeId);
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            // PHẢI LOAD LẠI GRADES KHI CÓ LỖI
            model.addAttribute("grades", gradeService.getAll());
            return "users/user-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        excelUserService.importExcel(file);
        return "redirect:/admin/users";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {

        ByteArrayInputStream in = excelUserService.exportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .body(new InputStreamResource(in));
    }
}