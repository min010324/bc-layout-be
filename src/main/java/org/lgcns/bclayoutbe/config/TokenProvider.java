package org.lgcns.bclayoutbe.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.common.Role;
import org.lgcns.bclayoutbe.exception.ErrorCode;
import org.lgcns.bclayoutbe.exception.TokenException;
import org.lgcns.bclayoutbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


@RequiredArgsConstructor
@Component
public class TokenProvider {

    @Value("${jwt.key}")
    private String key;
    private SecretKey secretKey;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60L; // 30분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60L * 24 * 7; // 7일
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "user_id";
    //  private final TokenService tokenService; // todo redis
    private final UserRepository userRepository;

    @PostConstruct
    private void setSecretKey() {
        secretKey = Keys.hmacShaKeyFor(key.getBytes());
    }

    public String generateAccessToken(OAuth2AuthenticationToken oauthToken, Integer userId, Role role) {
        return generateToken(oauthToken, userId, role, ACCESS_TOKEN_EXPIRE_TIME);
    }

    public String generateRefreshToken(OAuth2AuthenticationToken oauthToken, Integer userId, Role role) {
        String refreshToken = generateToken(oauthToken, userId, role, REFRESH_TOKEN_EXPIRE_TIME);
        var currentUser = userRepository.findById(userId);
        currentUser.ifPresentOrElse((temp) -> {
            temp.setRefreshToken(refreshToken);
            userRepository.save(temp);
        }, () -> {
            throw new TokenException(ErrorCode.INVALID_TOKEN);
        });
        return refreshToken;
    }

    private String generateToken(OAuth2AuthenticationToken authentication, Integer userId, Role role, long expireTime) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expireTime);
        var authority = ObjectUtils.isEmpty(role) ? Role.USER : role;

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(KEY_ROLE, authority.getType())
            .claim(KEY_USER_ID, userId)
            .setIssuedAt(now)
            .setExpiration(expiredDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, ACCESS_TOKEN_EXPIRE_TIME);
    }

    // 1. refresh token 발급
    public void generateRefreshToken(Authentication authentication, String accessToken) {
        String refreshToken = generateToken(authentication, REFRESH_TOKEN_EXPIRE_TIME);
        var user = (org.lgcns.bclayoutbe.entity.User) authentication.getPrincipal();
        var currentUser = userRepository.findById(user.getUserId());
        currentUser.ifPresentOrElse((temp) -> {
            temp.setRefreshToken(refreshToken);
            userRepository.save(temp);
        }, () -> {
            throw new TokenException(ErrorCode.INVALID_TOKEN);
        });
    }

    private String generateToken(String subject, String authorities,
        org.lgcns.bclayoutbe.entity.User user, long expireTime) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
            .setSubject(subject)
            .claim(KEY_ROLE, authorities)
            .claim(KEY_USER_ID, user.getUserId())
            .setIssuedAt(now)
            .setExpiration(expiredDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    private String generateToken(Authentication authentication, long expireTime) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expireTime);

        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining());
        var user = (org.lgcns.bclayoutbe.entity.User) authentication.getPrincipal();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(KEY_ROLE, authorities)
            .claim(KEY_USER_ID, user.getUserId())
            .setIssuedAt(now)
            .setExpiration(expiredDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

//    public Authentication getAuthentication(String token) {
//        Claims claims = parseClaims(token);
//        List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
//
//        // 2. security의 User 객체 생성
//        OAuth2User principal = new OAuth2User(claims.getSubject(), "", authorities);
//        return new OAuth2AuthenticationToken(principal, authorities);
//    }


    public Authentication getAuthentication(String token) { // todo
        Claims claims = parseClaims(token);
        List<SimpleGrantedAuthority> authorities = getAuthorities(claims);

        // 2. security의 User 객체 생성
        User principal = new User(claims.get(KEY_USER_ID).toString(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
        return Collections.singletonList(new SimpleGrantedAuthority(
            claims.get(KEY_ROLE).toString()));
    }

    private Integer getUserId(Claims claims) {
        return Integer.parseInt(claims.get(KEY_USER_ID).toString());
    }

    // 3. accessToken 재발급
    public String reissueAccessToken(String refreshToken) { //todo 처리 필요
        if (StringUtils.hasText(refreshToken) && validateToken(refreshToken)) {
            Claims claims = parseClaims(refreshToken);
            Integer userId = getUserId(claims);
            var user = userRepository.findById(userId).orElseThrow();
            var userRefreshToken = user.getRefreshToken();

            if (validateToken(userRefreshToken)) {
                String reissueAccessToken = generateAccessToken(getAuthentication(userRefreshToken));
                user.setRefreshToken(reissueAccessToken);
                userRepository.save(user);
                return reissueAccessToken;
            }
        }
//        throw new ServletException("invalid token"); // todo error 처리
        return null;
    }

    /**
     * 사용자 정보를 기반으로 OAuth2User 객체를 생성
     */
//    private OAuth2User loadOAuth2User(org.lgcns.bclayoutbe.entity.User user) {
//        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("id", user.getUserId());
//        attributes.put("email", user.getEmail());
//
//        return new DefaultOAuth2User(
//            Collections.singleton(new SimpleGrantedAuthority(user.getRole().getType())),
//            attributes,
//            "email"
//        );
//    }
//
//    /**
//     * OAuth2User를 기반으로 OAuth2AuthenticationToken 생성
//     */
//    private OAuth2AuthenticationToken createOAuth2Authentication(OAuth2User oAuth2User) {
//        return new OAuth2AuthenticationToken(
//            oAuth2User,
//            oAuth2User.getAuthorities(),
//            "slack" // registrationId (OAuth2 Provider 이름)
//        );
//    }
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        Claims claims = parseClaims(token);
        return claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (MalformedJwtException e) {
            throw new TokenException(ErrorCode.INVALID_TOKEN);
        } catch (SecurityException e) {
            throw new TokenException(ErrorCode.INVALID_JWT_SIGNATURE);
        }
    }
}
