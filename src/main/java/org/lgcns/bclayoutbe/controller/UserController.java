package org.lgcns.bclayoutbe.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.dto.UserDto;
import org.lgcns.bclayoutbe.dto.UserDto.Response;
import org.lgcns.bclayoutbe.service.AuthService;
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
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("")
    public ResponseEntity<UserDto.Response> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        var user = this.authService.getUser(Integer.valueOf(userDetails.getUsername()));
        return ResponseEntity.ok(user);
    }

    @PostMapping("")
    public ResponseEntity<UserDto.Response> updateMyInfo(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody UserDto.Request request) {
        var user = this.authService.updateUserInfo(Integer.valueOf(userDetails.getUsername()), request.getNickname(),
            request.getTeamName(), request.getProjectName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto.Response> getUser(@PathVariable Integer userId) {
        var user = this.authService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Response>> getMyInfo() {
        var res = this.authService.getUserList();
        return ResponseEntity.ok(res);
    }

}
