package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.model.request.ChapterRequest;
import com.company.mathapp_backend_04.model.response.ImportResult;
import com.company.mathapp_backend_04.repository.ChapterRepository;
import com.company.mathapp_backend_04.repository.SubjectRepository;
import com.company.mathapp_backend_04.service.ChapterService;
import com.company.mathapp_backend_04.service.SubjectService;
import com.company.mathapp_backend_04.service.admin.ExcelChapterService;
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

@Controller
@RequestMapping("/admin/chapters")
@RequiredArgsConstructor
public class AdminChapterController {

    private final ChapterService chapterService;
    private final SubjectService subjectService;
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final ExcelChapterService excelChapterService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "30") int size,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String message) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Chapter> chapterPage = chapterService.getAll(keyword, pageable);

        model.addAttribute("chapters", chapterPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjects", subjectService.getAll());
        model.addAttribute("activeMenu", "chapters");
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "pages/chapter-list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChapterRequest request, @RequestParam(required = false) Integer id) {
        try {
            if (id == null) {
                chapterService.addChapter(request);
            } else {
                chapterService.updateChapter(id, request);
            }
            return "redirect:/admin/chapters";
        } catch (Exception e) {
            return "redirect:/admin/chapters?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        chapterService.deleteChapter(id);
        return "redirect:/admin/chapters";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = excelChapterService.importExcel(file);
            return buildImportRedirect(result);
        } catch (Exception exception) {
            return "redirect:/admin/chapters?error=" + encode(exception.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream inputStream = excelChapterService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chapters.xlsx")
                .body(new InputStreamResource(inputStream));
    }

    private String buildImportRedirect(ImportResult result) {
        String message = "Import completed: " + result.getSuccess() + "/" + result.getTotal() + " rows processed";
        if (result.getError() > 0) {
            return "redirect:/admin/chapters?message=" + encode(message)
                    + "&error=" + encode("Failed rows: " + result.getError()
                    + (result.getErrorFilePath() != null ? ". Error file: " + result.getErrorFilePath() : ""));
        }
        return "redirect:/admin/chapters?message=" + encode(message);
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
