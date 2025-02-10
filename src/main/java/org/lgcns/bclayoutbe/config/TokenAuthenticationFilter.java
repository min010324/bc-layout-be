package org.lgcns.bclayoutbe.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lgcns.bclayoutbe.exception.ErrorCode;
import org.lgcns.bclayoutbe.exception.TokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        log.info("TokenAuthenticationFilter: {}", request);
        log.info("TokenAuthenticationFilter: {}", request.getHeader(AUTHORIZATION));
        log.info("TokenAuthenticationFilter: {}", response);
        String accessToken = resolveToken(request);
        // accessToken 검증
        if (tokenProvider.validateToken(accessToken)) {
            setAuthentication(accessToken);
        } else {
            // 만료되었을 경우 accessToken 재발급 // todo 개선 필요
//            String reissueAccessToken = tokenProvider.reissueAccessToken(accessToken);
//            if (StringUtils.hasText(reissueAccessToken)) {
//                setAuthentication(reissueAccessToken);
//                // 재발급된 accessToken 다시 전달
//                response.setHeader(AUTHORIZATION, TokenKey.TOKEN_PREFIX + reissueAccessToken);
//            }
            throw new TokenException(ErrorCode.INVALID_TOKEN);
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN");
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        if (ObjectUtils.isEmpty(token) || !token.startsWith(TokenKey.TOKEN_PREFIX)) {
            return null;
        }
        return token.substring(TokenKey.TOKEN_PREFIX.length());
    }

    // 이 부분!!
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return StringUtils.startsWithAny(request.getRequestURI(), "/api/auth/refresh");
    }
}
