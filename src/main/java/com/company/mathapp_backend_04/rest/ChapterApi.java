package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.dto.ChapterOverviewDTO;
import com.company.mathapp_backend_04.model.request.ChapterRequest;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.ChapterResponse;
import com.company.mathapp_backend_04.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/chapters")
@RestController
@RequiredArgsConstructor
public class ChapterApi {
    private final ChapterService chapterService;

    @GetMapping("/{subjectId}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterOverviewDTO>>> getChaptersInSubject(
            @RequestParam Integer userId,
            @PathVariable Integer subjectId
            ) {

        List<ChapterOverviewDTO> overviews = chapterService.getChaptersBySubject(userId, subjectId);

        ApiResponse<List<ChapterOverviewDTO>> response = new ApiResponse<>(
                200,
                "Lấy danh sách chương thành công",
                overviews
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subjectId}")
    public List<ChapterResponse> getChapters(@PathVariable Integer subjectId) {

        return chapterService.getChaptersBySubjectId(subjectId);
    }

    @PostMapping
    public ResponseEntity<?> addChapter(@Valid @RequestBody ChapterRequest chapterRequest) {
        chapterService.addChapter(chapterRequest);
        return ResponseEntity.ok("Chapter created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChapter(@PathVariable Integer id,
                                          @Valid @RequestBody ChapterRequest chapterRequest) {

        chapterService.updateChapter(id, chapterRequest);

        return ResponseEntity.ok("Chapter updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChapter(@PathVariable Integer id) {

        chapterService.deleteChapter(id);

        return ResponseEntity.ok("Chapter deleted successfully");
    }
}
