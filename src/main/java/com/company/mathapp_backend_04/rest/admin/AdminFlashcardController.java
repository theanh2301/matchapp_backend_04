package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Flashcard;
import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.model.request.FlashcardRequest;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.FlashcardRepository;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.service.FlashcardService;
import com.company.mathapp_backend_04.service.admin.ExcelFlashcardService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/admin/flashcards")
@RequiredArgsConstructor
public class AdminFlashcardController {

    private final FlashcardService flashcardService;
    private final LessonRepository lessonRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExcelFlashcardService excelFlashcardService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message,
                       Model model) {

        Page<Flashcard> flashcards = flashcardService.getAll(keyword, page, 50);

        model.addAttribute("flashcards", flashcards);
        model.addAttribute("keyword", keyword);
        model.addAttribute("lessons", lessonRepository.findAll());
        model.addAttribute("activeMenu", "flashcards");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/flashcard-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute FlashcardRequest request, @RequestParam(required = false) Integer id) {
        try {
            if (id == null) {
                flashcardService.addFlashcard(request);
            } else {
                flashcardService.updateFlashcard(id, request);
            }
            return "redirect:/admin/flashcards";
        } catch (Exception e) {
            return "redirect:/admin/flashcards?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        flashcardService.deleteFlashcard(id);
        return "redirect:/admin/flashcards";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Integer> ids) {
        flashcardService.deleteBulk(ids);
        return "redirect:/admin/flashcards";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = excelFlashcardService.importExcel(file);
            return buildImportRedirect(result);
        } catch (Exception exception) {
            return "redirect:/admin/flashcards?error=" + encode(exception.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream inputStream = excelFlashcardService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=flashcards.xlsx")
                .body(new InputStreamResource(inputStream));
    }

    private String buildImportRedirect(ImportResult result) {
        String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
        if (result.getError() > 0) {
            return "redirect:/admin/flashcards?message=" + encode(message)
                    + "&error=" + encode("Failed rows: " + result.getError()
                    + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
        }
        return "redirect:/admin/flashcards?message=" + encode(message);
    }

    private String getCellValue(Row row, int columnIndex) {
        if (row.getCell(columnIndex) == null) {
            return null;
        }
        return new org.apache.poi.ss.usermodel.DataFormatter().formatCellValue(row.getCell(columnIndex)).trim();
    }

    private Integer getIntegerCellValue(Row row, int columnIndex) {
        String value = getCellValue(row, columnIndex);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private boolean isBlankRow(Row row, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            String value = getCellValue(row, index);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
