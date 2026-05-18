package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.QuizQuestion;
import com.company.mathapp_backend_04.model.dto.AdminQuizQuestionView;
import com.company.mathapp_backend_04.model.request.QuizAnswerRequest;
import com.company.mathapp_backend_04.model.request.QuizQuestionRequest;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.repository.QuizAnswerRepository;
import com.company.mathapp_backend_04.repository.QuizQuestionRepository;
import com.company.mathapp_backend_04.service.QuizQuestionService;
import com.company.mathapp_backend_04.service.admin.ExcelQuizQuestionService;
import com.company.mathapp_backend_04.util.CsvImportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URLEncoder;

@Controller
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class AdminQuizQuestionController {

    private final QuizQuestionService quizQuestionService;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final LessonRepository lessonRepository;
    private final ExcelQuizQuestionService excelQuizQuestionService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<QuizQuestion> questions = quizQuestionService.getQuestions(keyword, pageable);
        Map<Integer, List<com.company.mathapp_backend_04.entity.QuizAnswer>> answersByQuestionId = quizAnswerRepository
                .findByQuizQuestionIdIn(questions.getContent().stream().map(QuizQuestion::getId).toList())
                .stream()
                .sorted(Comparator.comparing(com.company.mathapp_backend_04.entity.QuizAnswer::getId))
                .collect(Collectors.groupingBy(answer -> answer.getQuizQuestion().getId(), LinkedHashMap::new, Collectors.toList()));

        List<AdminQuizQuestionView> rows = questions.getContent().stream()
                .map(q -> new AdminQuizQuestionView(
                        q.getId(),
                        q.getContent(),
                        q.getXpReward(),
                        q.getLesson().getId(),
                        q.getLesson().getLessonName(),
                        answersByQuestionId.getOrDefault(q.getId(), List.of())
                ))
                .toList();

        model.addAttribute("questions", new PageImpl<>(rows, pageable, questions.getTotalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("lessons", lessonRepository.findAll());
        model.addAttribute("activeMenu", "quiz-questions");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/quiz-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute QuizQuestionRequest request, @RequestParam(required = false) Integer id) {
        try {
            if (id == null) {
                quizQuestionService.addQuestionAndAnswer(request);
            } else {
                quizQuestionService.updateQuestionAndAnswer(id, request);
            }
            return "redirect:/admin/questions";
        } catch (Exception e) {
            return "redirect:/admin/questions?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        quizQuestionService.deleteQuestionAnswer(id);
        return "redirect:/admin/questions";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Integer> ids) {
        if (ids != null) {
            ids.forEach(quizQuestionService::deleteQuestionAnswer);
        }
        return "redirect:/admin/questions";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quiz-questions.xlsx")
                .body(new InputStreamResource(excelQuizQuestionService.exportExcel()));
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = excelQuizQuestionService.importExcel(file);
            return buildImportRedirect(result);
        } catch (Exception e) {
            return "redirect:/admin/questions?error=" + encode(e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String buildImportRedirect(ImportResult result) {
        String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
        if (result.getError() > 0) {
            return "redirect:/admin/questions?message=" + encode(message)
                    + "&error=" + encode("Failed rows: " + result.getError()
                    + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
        }
        return "redirect:/admin/questions?message=" + encode(message);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
