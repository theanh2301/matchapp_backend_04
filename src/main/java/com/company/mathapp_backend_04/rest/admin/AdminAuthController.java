package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final FlashcardRepository flashcardRepository;
    private final MatchCardRepository matchCardRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final PracticeRepository practiceRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping({"", "/"})
    public String dashboardShell(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("contentUrl", "/admin/home-content");
        return "admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("contentUrl", "/admin/home-content");
        return "admin/dashboard";
    }

    @GetMapping("/home-content")
    public String dashboardHome(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("subjectCount", subjectRepository.count());
        model.addAttribute("chapterCount", chapterRepository.count());
        model.addAttribute("lessonCount", lessonRepository.count());
        model.addAttribute("flashcardCount", flashcardRepository.count());
        model.addAttribute("matchcardCount", matchCardRepository.count());
        model.addAttribute("quizQuestionCount", quizQuestionRepository.count());
        model.addAttribute("quizAnswerCount", quizAnswerRepository.count());
        model.addAttribute("practiceCount", practiceRepository.count());
        model.addAttribute("practiceQuestionCount", practiceQuestionRepository.count());
        model.addAttribute("practiceAnswerCount", practiceAnswerRepository.count());
        return "admin/home-content";
    }
}
