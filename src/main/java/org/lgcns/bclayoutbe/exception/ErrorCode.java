package org.lgcns.bclayoutbe.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // auth
    ILLEGAL_REGISTRATION_ID(NOT_ACCEPTABLE, "illegal registration id"),
    TOKEN_EXPIRED(UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(UNAUTHORIZED, "올바르지 않은 토큰입니다."),
    INVALID_JWT_SIGNATURE(UNAUTHORIZED, "올바르지 않은 토큰 signature 입니다."),

    // seat
    DUPLICATE_USE_SEAT(INTERNAL_SERVER_ERROR, "이미 사용 중인 자리가 존재합니다."),

    // label
    CANNOT_FIND_LABEL(INTERNAL_SERVER_ERROR, "라벨이 존재하지 않습니다. 관리자에게 문의해주세요");

    private final HttpStatus httpStatus;
    private final String message;
}
