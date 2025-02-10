package org.lgcns.bclayoutbe.controller;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.dto.LabelDto;
import org.lgcns.bclayoutbe.service.LabelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/label")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping()
    public ResponseEntity<List<LabelDto.Response>> getLabelList(@AuthenticationPrincipal UserDetails userDetails) {
        List<LabelDto.Response> res = labelService.getLabelList(Integer.valueOf(userDetails.getUsername()));
        return ResponseEntity.ok(res);
    }

    @PostMapping()
    public ResponseEntity<LabelDto.Response> updateLabel(@AuthenticationPrincipal UserDetails userDetails,
        @RequestBody LabelDto.Request request) {
        LabelDto.Response res = labelService.updateLabel(Integer.valueOf(userDetails.getUsername()),
            request.getLabelId(), request.getName(),
            request.getUserList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/labelId")
    public ResponseEntity<LabelDto.Response> getLabelInfo(@PathVariable Integer labelId) {
        LabelDto.Response res = labelService.getLabelInfo(labelId);
        return ResponseEntity.ok(res);
    }

}
