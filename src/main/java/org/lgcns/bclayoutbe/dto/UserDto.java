package org.lgcns.bclayoutbe.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lgcns.bclayoutbe.entity.User;

public class UserDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {

        private Integer userId;
        private String name;
        private String nickname;
        private String email;
        private String role;
        private String team;
        private String project;

        public static UserDto.Response fromUser(final User user) {
            if (user == null) {
                return null;
            }
            return Response.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole().getType())
                .team(user.getTeam().getName())  // null 처리 메서드 호출
                .project(user.getProject().getName())  // null 처리 메서드 호출
                .build();

        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Request {

        private String nickname;
        private String teamName;
        private String projectName;
    }
}
