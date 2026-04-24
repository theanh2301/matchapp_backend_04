package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.model.request.ChapterRequest;
import com.company.mathapp_backend_04.service.ChapterService;
import com.company.mathapp_backend_04.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/chapters")
@RequiredArgsConstructor
public class AdminChapterController {

    private final ChapterService chapterService;
    private final SubjectService subjectService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "30") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Chapter> chapterPage = chapterService.getAll(keyword, pageable);

        model.addAttribute("chapters", chapterPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjects", subjectService.getAll());

        return "pages/chapter-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChapterRequest request,
                       @RequestParam(required = false) Integer id,
                       Model model) {

        try {
            if (id == null) {
                chapterService.addChapter(request);
            } else {
                chapterService.updateChapter(id, request);
            }
        } catch (Exception e) {
            return "redirect:/admin/chapters?error=" + e.getMessage();
        }

        return "redirect:/admin/chapters";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        chapterService.deleteChapter(id);
        return "redirect:/admin/chapters";
    }
}