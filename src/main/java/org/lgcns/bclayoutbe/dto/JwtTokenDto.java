package org.lgcns.bclayoutbe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JwtTokenDto {

    private String grantType;
    private String accessToken;
    private String refreshToken;
}
