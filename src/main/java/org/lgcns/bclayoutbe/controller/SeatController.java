package org.lgcns.bclayoutbe.controller;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.dto.SeatDto;
import org.lgcns.bclayoutbe.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping()
    public ResponseEntity<ArrayList<SeatDto.Response>> getSeatList() {
        var res = this.seatService.getAllSeat();
        return ResponseEntity.ok(res);
    }

    @PostMapping()
    public ResponseEntity<SeatDto.Response> updateSeat(@RequestBody SeatDto.Request request,
        @AuthenticationPrincipal UserDetails userDetails) {
        var res = this.seatService.updateSeatInfo(request.getSeatId(), Integer.valueOf(userDetails.getUsername()),
            request.getUseYn(), request.getEndDate().atStartOfDay());
        return ResponseEntity.ok(res);

    }
}
