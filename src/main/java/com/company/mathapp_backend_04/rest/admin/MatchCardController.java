package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.model.request.MatchCardPairRequest;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.model.response.MatchCardPairResponse;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.repository.MatchCardRepository;
import com.company.mathapp_backend_04.service.MatchCardService;
import com.company.mathapp_backend_04.service.admin.ExcelMatchCardService;
import com.company.mathapp_backend_04.util.CsvImportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

@Controller
@RequestMapping("/admin/match-cards")
@RequiredArgsConstructor
public class MatchCardController {

    private final MatchCardService matchCardService;
    private final LessonRepository lessonRepository;
    private final MatchCardRepository matchCardRepository;
    private final ExcelMatchCardService excelMatchCardService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @PageableDefault(size = 10, sort = "pairId") Pageable pageable,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message,
                       Model model) {
        Page<MatchCardPairResponse> pairs = matchCardService.getAllCardPairs(keyword, pageable);
        List<Lesson> lessons = lessonRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        model.addAttribute("pairs", pairs);
        model.addAttribute("lessons", lessons);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "matchcards");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/match-card-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute MatchCardPairRequest request,
                       @RequestParam(defaultValue = "false") boolean isEdit) {
        try {
            if (isEdit) {
                matchCardService.updateMatchCardPair(request);
            } else {
                if (request.getPairId() == null) {
                    request.setPairId(matchCardService.generatePairId(request.getLessonId()));
                }
                matchCardService.addMatchCardPair(request);
            }
            return "redirect:/admin/match-cards";
        } catch (Exception e) {
            return "redirect:/admin/match-cards?error=" + e.getMessage();
        }
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Integer pairId, @RequestParam Integer lessonId) {
        matchCardService.deleteMatchCardPair(pairId, lessonId);
        return "redirect:/admin/match-cards";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false, name = "selectedPairs") List<String> selectedPairs) {
        matchCardService.deleteMultipleByTokens(selectedPairs);
        return "redirect:/admin/match-cards";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=match-cards.xlsx")
                .body(new InputStreamResource(excelMatchCardService.exportExcel()));
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = excelMatchCardService.importExcel(file);
            return buildImportRedirect(result);
        } catch (Exception e) {
            return "redirect:/admin/match-cards?error=" + encode(e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String buildImportRedirect(ImportResult result) {
        String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
        if (result.getError() > 0) {
            return "redirect:/admin/match-cards?message=" + encode(message)
                    + "&error=" + encode("Failed rows: " + result.getError()
                    + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
        }
        return "redirect:/admin/match-cards?message=" + encode(message);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean lessonExistsWithPair(Integer lessonId, Integer pairId) {
        return lessonRepository.findById(lessonId)
                .map(lesson -> matchCardRepository.existsByPairIdAndLesson(pairId, lesson))
                .orElse(false);
    }
}
