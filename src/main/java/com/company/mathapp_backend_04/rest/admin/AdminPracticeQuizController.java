package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.PracticeQuestion;
import com.company.mathapp_backend_04.model.dto.AdminPracticeQuestionView;
import com.company.mathapp_backend_04.model.request.PracticeAnswerRequest;
import com.company.mathapp_backend_04.model.request.PracticeQuestionRequest;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.PracticeAnswerRepository;
import com.company.mathapp_backend_04.repository.PracticeQuestionRepository;
import com.company.mathapp_backend_04.repository.PracticeRepository;
import com.company.mathapp_backend_04.service.PracticeQuestionService;
import com.company.mathapp_backend_04.service.admin.ExcelPracticeQuestionService;
import com.company.mathapp_backend_04.util.CsvImportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/admin/practice-questions")
@RequiredArgsConstructor
public class AdminPracticeQuizController {

    private final PracticeQuestionService practiceQuestionService;
    private final PracticeAnswerRepository practiceAnswerRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeRepository practiceRepository;
    private final ExcelPracticeQuestionService excelPracticeQuestionService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message,
                       Model model) {

        Page<PracticeQuestion> questions = practiceQuestionService.getAll(keyword, page, 20);
        Map<Integer, List<com.company.mathapp_backend_04.entity.PracticeAnswer>> answersByQuestionId = practiceAnswerRepository
                .findByPracticeQuestionIdIn(questions.getContent().stream().map(PracticeQuestion::getId).toList())
                .stream()
                .sorted(Comparator.comparing(com.company.mathapp_backend_04.entity.PracticeAnswer::getId))
                .collect(Collectors.groupingBy(answer -> answer.getPracticeQuestion().getId(), LinkedHashMap::new, Collectors.toList()));

        List<AdminPracticeQuestionView> rows = questions.getContent().stream()
                .map(q -> new AdminPracticeQuestionView(
                        q.getId(),
                        q.getContent(),
                        q.getXpReward(),
                        q.getDifficulty(),
                        q.getPractice().getId(),
                        q.getPractice().getTitle(),
                        answersByQuestionId.getOrDefault(q.getId(), List.of())
                ))
                .toList();

        model.addAttribute("questions", new PageImpl<>(rows, questions.getPageable(), questions.getTotalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("practices", practiceRepository.findAll());
        model.addAttribute("activeMenu", "practice-questions");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/practice-question-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute PracticeQuestionRequest request, @RequestParam(required = false) Integer id) {
        try {
            if (id == null) {
                practiceQuestionService.addQuestionAndAnswer(request);
            } else {
                practiceQuestionService.updateQuestionAndAnswer(id, request);
            }
            return "redirect:/admin/practice-questions";
        } catch (Exception e) {
            return "redirect:/admin/practice-questions?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        practiceQuestionService.deleteQuestion(id);
        return "redirect:/admin/practice-questions";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Integer> ids) {
        practiceQuestionService.deleteBulk(ids);
        return "redirect:/admin/practice-questions";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=practice-questions.xlsx")
                .body(new InputStreamResource(excelPracticeQuestionService.exportExcel()));
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = excelPracticeQuestionService.importExcel(file);
            return buildImportRedirect(result);
        } catch (Exception e) {
            return "redirect:/admin/practice-questions?error=" + encode(e.getMessage());
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
            return "redirect:/admin/practice-questions?message=" + encode(message)
                    + "&error=" + encode("Failed rows: " + result.getError()
                    + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
        }
        return "redirect:/admin/practice-questions?message=" + encode(message);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
