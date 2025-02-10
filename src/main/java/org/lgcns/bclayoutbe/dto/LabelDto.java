package org.lgcns.bclayoutbe.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lgcns.bclayoutbe.entity.Label;
import org.lgcns.bclayoutbe.entity.LabelUser;

public class LabelDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {

        private Integer labelId;
        private String name;
        private List<UserDto.Response> users;

        public static LabelDto.Response fromLabel(Label label) {
            List<UserDto.Response> users = label.getLabelUserList().stream()
                .map(item -> UserDto.Response.fromUser(item.getUser()))
                .toList();
            return Response.builder()
                .labelId(label.getLabelId())
                .name(label.getName())
                .users(users)
                .build();
        }

        public static LabelDto.Response fromLabelUser(List<LabelUser> labelUser) {
            Label label = labelUser.getFirst().getLabel();
            List<UserDto.Response> users = labelUser.stream()
                .map(item -> UserDto.Response.fromUser(item.getUser()))
                .toList();
            return Response.builder()
                .labelId(label.getLabelId())
                .name(label.getName())
                .users(users)
                .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Request {

        private Integer labelId;
        private String name;
        private List<Integer> userList;
    }

}
