package com.company.mathapp_backend_04.rest.admin;

import com.company.mathapp_backend_04.model.request.MatchCardPairRequest;
import com.company.mathapp_backend_04.model.response.MatchCardPairResponse;
import com.company.mathapp_backend_04.model.response.MatchCardResponse;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.service.MatchCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/match-cards")
@RequiredArgsConstructor
public class MatchCardController {

    private final MatchCardService matchCardService;
    private final LessonRepository lessonRepository;

    @GetMapping
    public String list(
            @RequestParam Integer lessonId,
            Model model) {

        List<MatchCardPairResponse> pairs = matchCardService.getMatchCardPair(lessonId);

        model.addAttribute("pairs", pairs);
        model.addAttribute("lessonId", lessonId);

        return "pages/match-card-list";
    }

    @PostMapping("/save")
    public String save(MatchCardPairRequest request) {

        if (request.getPairId() == null) {
            request.setPairId((int) (System.currentTimeMillis() % 100000));
        }

        try {
            matchCardService.addMatchCardPair(request);
        } catch (Exception e) {
            matchCardService.updateMatchCardPair(request);
        }

        return "redirect:/admin/match-cards";
    }

    @GetMapping("/delete")
    public String delete(
            @RequestParam Integer pairId,
            @RequestParam Integer lessonId) {

        matchCardService.deleteMatchCardPair(pairId, lessonId);

        return "redirect:/admin/match-cards";
    }

    @PostMapping("/bulk-delete")
    public String bulkDelete(
            @RequestParam List<Integer> pairIds,
            @RequestParam Integer lessonId) {

        matchCardService.deleteMultiple(pairIds, lessonId);

        return "redirect:/admin/match-cards";
    }
}