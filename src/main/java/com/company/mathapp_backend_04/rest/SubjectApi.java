package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.dto.SubjectOverviewDTO;
import com.company.mathapp_backend_04.model.dto.SubjectProgressDTO;
import com.company.mathapp_backend_04.model.request.SubjectRequest;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.SubjectResponse;
import com.company.mathapp_backend_04.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectApi {

    private final SubjectService subjectService;

    @GetMapping
    public List<SubjectResponse> getSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/progress")
    public List<SubjectProgressDTO> getProgress(@RequestParam Integer userId) {
        return subjectService.getSubjectProgress(userId);
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<List<SubjectOverviewDTO>>> getSubjectOverviews(@RequestParam Integer userId) {

        List<SubjectOverviewDTO> overviews = subjectService.getSubjectOverviews(userId);

        ApiResponse<List<SubjectOverviewDTO>> response = new ApiResponse<>(
                200,
                "Lấy danh sách môn học thành công",
                overviews
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addSubject(@Valid @RequestBody SubjectRequest request) {
        subjectService.addSubject(request);
        return ResponseEntity.ok("Subject created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable Integer id,
                                          @Valid @RequestBody SubjectRequest subjectRequest) {

        subjectService.updateSubject(id, subjectRequest);

        return ResponseEntity.ok("Subject updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {

        subjectService.deleteSubject(id);

        return ResponseEntity.ok("Subject deleted successfully");
    }
}
