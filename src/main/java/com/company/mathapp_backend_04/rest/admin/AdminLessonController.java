package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.model.request.LessonRequest;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.ChapterRepository;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.service.LessonService;
import com.company.mathapp_backend_04.service.admin.ExcelLessonService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/admin/lessons")
@RequiredArgsConstructor
public class AdminLessonController {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final LessonService lessonService;
    private final ExcelLessonService excelLessonService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "100") int size,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Lesson> lessons = keyword.isEmpty()
                ? lessonRepository.findAll(pageable)
                : lessonRepository.findByLessonNameContainingIgnoreCase(keyword, pageable);

        model.addAttribute("lessons", lessons);
        model.addAttribute("keyword", keyword);
        model.addAttribute("chapters", chapterRepository.findAll());
        model.addAttribute("activeMenu", "lessons");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/lesson-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LessonRequest request, @RequestParam(required = false) Integer id) {
        try {
            if (id == null) {
                lessonService.addLesson(request);
            } else {
                lessonService.updateLesson(id, request);
            }
            return "redirect:/admin/lessons";
        } catch (Exception e) {
            return "redirect:/admin/lessons?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        lessonService.deleteLesson(id);
        return "redirect:/admin/lessons";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Integer> ids) {
        lessonService.deleteBulk(ids);
        return "redirect:/admin/lessons";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = excelLessonService.importExcel(file);
            return buildImportRedirect(result);
        } catch (Exception exception) {
            return "redirect:/admin/lessons?error=" + encode(exception.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream inputStream = excelLessonService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lessons.xlsx")
                .body(new InputStreamResource(inputStream));
    }

    private String buildImportRedirect(ImportResult result) {
        String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
        if (result.getError() > 0) {
            return "redirect:/admin/lessons?message=" + encode(message)
                    + "&error=" + encode("Failed rows: " + result.getError()
                    + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
        }
        return "redirect:/admin/lessons?message=" + encode(message);
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

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
