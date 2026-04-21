package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.service.GradeService;
import com.company.mathapp_backend_04.service.admin.AdminSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/subjects")
@RequiredArgsConstructor
public class AdminSubjectController {
    private final AdminSubjectService subjectService;
    private final GradeService gradeService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        String safeKeyword = keyword.trim();

        model.addAttribute("keyword", safeKeyword);
        model.addAttribute("subjects", subjectService.getAll(safeKeyword, pageable));
        model.addAttribute("activeMenu", "subjects");

        return "subjects/subject-list";
    }
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("subject", new Subject());
        model.addAttribute("grades", gradeService.getAll());
        return "subjects/subject-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Subject subject, @RequestParam Integer gradeId) {
        subjectService.save(subject, gradeId);
        return "redirect:/admin/subjects";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        model.addAttribute("subject", subjectService.getById(id));
        model.addAttribute("grades", gradeService.getAll());
        return "subjects/subject-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        subjectService.delete(id);
        return "redirect:/admin/subjects";
    }
}