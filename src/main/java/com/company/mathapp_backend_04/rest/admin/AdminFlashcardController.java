package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Flashcard;
import com.company.mathapp_backend_04.model.request.FlashcardRequest;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/flashcards")
@RequiredArgsConstructor
public class AdminFlashcardController {

    private final FlashcardService flashcardService;
    private final LessonRepository lessonRepository;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        Page<Flashcard> flashcards = flashcardService.getAll(keyword, page, 50);

        model.addAttribute("flashcards", flashcards);
        model.addAttribute("keyword", keyword);
        model.addAttribute("lessons", lessonRepository.findAll());

        return "pages/flashcard-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute FlashcardRequest request,
                       @RequestParam(required = false) Integer id) {

        try {
            if (id == null) {
                flashcardService.addFlashcard(request);
            } else {
                flashcardService.updateFlashcard(id, request);
            }
        } catch (Exception e) {
            return "redirect:/admin/flashcards?error=" + e.getMessage();
        }

        return "redirect:/admin/flashcards";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        flashcardService.deleteFlashcard(id);
        return "redirect:/admin/flashcards";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam List<Integer> ids) {
        flashcardService.deleteBulk(ids);
        return "redirect:/admin/flashcards";
    }
}