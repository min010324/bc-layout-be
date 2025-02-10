package org.lgcns.bclayoutbe.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Possible {
    /**
     * 성공 문자열 형식
     */
    TRUE("1", "Y", true),
    /**
     * 실패 문자열 형식
     */
    FALSE("0", "N", false);

    private final String resultNum;
    private final String resultAlphabet;
    private final boolean isResult;
}
