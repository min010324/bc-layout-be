package org.lgcns.bclayoutbe.config;

import java.util.Map;
import lombok.Builder;
import org.lgcns.bclayoutbe.common.Role;
import org.lgcns.bclayoutbe.entity.User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Builder
public record OAuth2UserInfo(
    String name,
    String email,
    String profile,
    String sub
) {

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes)
        throws OAuth2AuthenticationException {
        return switch (registrationId) { // registration id별로 userInfo 생성
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "slack" -> ofSlack(attributes);
            default -> throw new OAuth2AuthenticationException("ILLEGAL_REGISTRATION_ID");
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
            .name((String) attributes.get("name"))
            .email((String) attributes.get("email"))
            .profile((String) attributes.get("picture"))
            .build();
    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
            .name((String) profile.get("nickname"))
            .email((String) account.get("email"))
            .profile((String) profile.get("profile_image_url"))
            .build();
    }

    private static OAuth2UserInfo ofSlack(Map<String, Object> attributes) {

        return OAuth2UserInfo.builder()
            .name((String) attributes.get("name"))
            .email((String) attributes.get("email"))
            .profile((String) attributes.get("picture"))
            .sub((String) attributes.get("sub"))
            .build();
    }

    public User toEntity() {
        return User.builder()
            .name(name)
            .email(email)
            .sub(sub)
            .role(Role.USER)
            .build();
    }
}
