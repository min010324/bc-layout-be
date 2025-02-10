package org.lgcns.bclayoutbe.config;


import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
//        return web -> web.ignoring()
//            // error endpoint를 열어줘야 함, favicon.ico 추가!
//            .requestMatchers("/error", "/favicon.ico");
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // rest api 설정
            .csrf(
                AbstractHttpConfigurer::disable) // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
//            .cors(AbstractHttpConfigurer::disable) // todo cors 비활성화 -> 프론트와 연결 시 따로 설정 필요
            .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
            .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성화
            .formLogin(AbstractHttpConfigurer::disable) // 기본 login form 비활성화
            .logout(AbstractHttpConfigurer::disable) // 기본 logout 비활성화

            .headers(c -> c.frameOptions(
                FrameOptionsConfig::disable).disable()) // X-Frame-Options 비활성화
            .sessionManagement(c ->
                c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않음

            // request 인증, 인가 설정
            .authorizeHttpRequests(request -> request
                    .requestMatchers(
                        new AntPathRequestMatcher("/api/auth/refresh"),
                        new AntPathRequestMatcher("/error"),  // 인증 없이 접근 허용
                        new AntPathRequestMatcher("/favicon.ico"),
                        new AntPathRequestMatcher("/oauth2/**")
                    ).permitAll()
                    .requestMatchers("/api/user/**").hasRole("USER")
//                .requestMatchers("/api/seat/**").hasRole("USER")
                    .requestMatchers("/api/admin").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )

            // oauth2 설정
            .oauth2Login(oauth -> oauth// OAuth2 로그인 기능에 대한 여러 설정의 진입점
                    // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정을 담당
                    .userInfoEndpoint(c -> c.userService(oAuth2UserService))
//                .loginProcessingUrl("/api/v1/oauth2/*")
                    .successHandler(oAuth2SuccessHandler) // 로그인 성공 시 핸들러
                    .failureHandler(new OAuth2FailureHandler()) // 로그인 실패 시 핸들러
            )

            // jwt 관련 설정
            .addFilterBefore(tokenAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new TokenExceptionFilter(),
                tokenAuthenticationFilter.getClass()) // 토큰 예외 핸들링

            // 인증 예외 핸들링
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }

    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowedOriginPatterns(Collections.singletonList("*")); // 허용할 origin
//            config.setAllowedOrigins(Collections.singletonList("http://localhost:5173")); // 허용할 origin
            config.setAllowCredentials(true);
            return config;
        };
    }
}
