package org.lgcns.bclayoutbe.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private static final String URI = "http://localhost:5173/";
//    private static final String FAIL_URI = "http://localhost:5173/login";
//    private static final String SUCCESS_URI = "http://localhost:5173/seat";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        // accessToken, refreshToken 발급
        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauthUser = oauthToken.getPrincipal();
            var user = this.authService.signUp(oauthToken.getAuthorizedClientRegistrationId(),
                oauthUser.getAttributes());
            String accessToken = tokenProvider.generateAccessToken(oauthToken, user.getUserId(), user.getRole());
            String refreshToken = tokenProvider.generateRefreshToken(oauthToken, user.getUserId(), user.getRole());
            String redirectUrl = UriComponentsBuilder.fromUriString(URI)
                .queryParam("redirectedFromSocialLogin", true)
                .queryParam("accessToken", accessToken) // todo 개선 필요
                .build().toUriString();

//            response.addHeader(AUTHORIZATION, accessToken); // 응답을 프론트에서 찾아올 방법이 없음
//            response.addCookie(createCookie("accessToken", accessToken)); // 프론트에서 가져올 방법이 없음
            response.addCookie(createCookie("refreshToken", refreshToken));
            response.sendRedirect(redirectUrl);
            return;
        }
//        String accessToken  = tokenProvider.generateAccessToken(authentication);
//        tokenProvider.generateRefreshToken(authentication, accessToken);
        // 토큰 전달을 위한 redirect
        String redirectUrl = UriComponentsBuilder.fromUriString(URI)
            .queryParam("redirectedFromSocialLogin", false)
            .build().toUriString();
        response.sendRedirect(redirectUrl);
    }

    private Cookie createCookie(String cookieName, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        // 쿠키 속성 설정
        cookie.setHttpOnly(true);  //httponly 옵션 설정
//        cookie.setSecure(true); //https 옵션 설정
        cookie.setSecure(false); //http 옵션 설정 로컬 개발 용
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge(60 * 60 * 24); //쿠키 만료시간 설정
//        cookie.setAttribute("SameSite", "None"); // local 개발 용
        return cookie;
    }
}
