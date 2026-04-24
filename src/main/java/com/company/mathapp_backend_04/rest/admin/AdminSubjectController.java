package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.service.GradeService;
import com.company.mathapp_backend_04.service.admin.AdminSubjectService;
import com.company.mathapp_backend_04.service.admin.ExcelSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/admin/subjects")
@RequiredArgsConstructor
public class AdminSubjectController {

    private final AdminSubjectService subjectService;
    private final GradeService gradeService;
    private final ExcelSubjectService excelSubjectService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        Page<Subject> subjects = subjectService.getAll(keyword, pageable);

        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", gradeService.getAll()); // ⚠️ bắt buộc cho modal
        model.addAttribute("keyword", keyword);

        return "pages/subject-list";
    }

    @PostMapping("/save")
    public String save(
            @ModelAttribute Subject subject,
            @RequestParam Integer gradeId
    ) {
        subjectService.save(subject, gradeId);

        return "redirect:/admin/subjects";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        subjectService.delete(id);
        return "redirect:/admin/subjects";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam List<Integer> ids) {
        subjectService.deleteBulk(ids);
        return "redirect:/admin/subjects";
    }

   /* @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        excelSubjectService.importExcel(file);
        return "redirect:/admin/subjects";
    }*/

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {

        ByteArrayInputStream in = excelSubjectService.exportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subjects.xlsx")
                .body(new InputStreamResource(in));
    }
}