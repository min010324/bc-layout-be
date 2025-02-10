package org.lgcns.bclayoutbe.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/refresh") // todo cookie 전달이 안됨
    public ResponseEntity<String> reissueToken(
        @CookieValue(value = "refreshToken", required = false) String refreshToken,
        HttpServletRequest request) { // todo
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String temp = cookie.getValue();
                    System.out.println("refreshToken: " + temp); // 🔥 디버깅 로그
                    return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
                }
            }
        }

        String accessToken = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(accessToken);
    }
}
