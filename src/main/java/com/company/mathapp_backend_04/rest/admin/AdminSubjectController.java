package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.model.response.ImportResult;
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

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/subjects")
@RequiredArgsConstructor
public class AdminSubjectController {

    private final AdminSubjectService subjectService;
    private final GradeService gradeService;
    private final ExcelSubjectService excelSubjectService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @PageableDefault(size = 10) Pageable pageable,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message,
                       Model model) {
        Page<Subject> subjects = subjectService.getAll(keyword, pageable);

        model.addAttribute("subjects", subjects);
        model.addAttribute("grades", gradeService.getAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "subjects");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/subject-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Subject subject, @RequestParam Integer gradeId) {
        try {
            subjectService.save(subject, gradeId);
            return "redirect:/admin/subjects";
        } catch (Exception exception) {
            return "redirect:/admin/subjects?error=" + exception.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        subjectService.delete(id);
        return "redirect:/admin/subjects";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Integer> ids) {
        subjectService.deleteBulk(ids);
        return "redirect:/admin/subjects";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            ImportResult result = excelSubjectService.importExcel(file);
            String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
            if (result.getError() > 0) {
                return "redirect:/admin/subjects?message=" + encode(message)
                        + "&error=" + encode("Failed rows: " + result.getError()
                        + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
            }
            return "redirect:/admin/subjects?message=" + encode(message);
        } catch (Exception exception) {
            return "redirect:/admin/subjects?error=" + encode(exception.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream inputStream = excelSubjectService.exportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subjects.xlsx")
                .body(new InputStreamResource(inputStream));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
