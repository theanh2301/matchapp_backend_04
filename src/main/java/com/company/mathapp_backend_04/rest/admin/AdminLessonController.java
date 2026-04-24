package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.model.request.LessonRequest;
import com.company.mathapp_backend_04.repository.ChapterRepository;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/lessons")
@RequiredArgsConstructor
public class AdminLessonController {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final LessonService lessonService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "100") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Lesson> lessons = keyword.isEmpty()
                ? lessonRepository.findAll(pageable)
                : lessonRepository.findByLessonNameContainingIgnoreCase(keyword, pageable);

        model.addAttribute("lessons", lessons);
        model.addAttribute("keyword", keyword);
        model.addAttribute("chapters", chapterRepository.findAll());

        return "pages/lesson-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LessonRequest request,
                       @RequestParam(required = false) Integer id) {

        try {
            if (id == null) {
                lessonService.addLesson(request);
            } else {
                lessonService.updateLesson(id, request);
            }
        } catch (Exception e) {
            return "redirect:/admin/lessons?error=" + e.getMessage();
        }

        return "redirect:/admin/lessons";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        lessonRepository.deleteById(id);
        return "redirect:/admin/lessons";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam List<Integer> ids) {
        lessonRepository.deleteAllById(ids);
        return "redirect:/admin/lessons";
    }
}