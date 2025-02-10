package org.lgcns.bclayoutbe.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lgcns.bclayoutbe.config.OAuth2UserInfo;
import org.lgcns.bclayoutbe.config.TokenProvider;
import org.lgcns.bclayoutbe.dto.UserDto;
import org.lgcns.bclayoutbe.entity.Project;
import org.lgcns.bclayoutbe.entity.Team;
import org.lgcns.bclayoutbe.entity.User;
import org.lgcns.bclayoutbe.repository.ProjectRepository;
import org.lgcns.bclayoutbe.repository.TeamRepository;
import org.lgcns.bclayoutbe.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final TokenProvider tokenProvider;

    public UserDto.Response getUser(Integer userId) {
        User user = this.userRepository.findByIdWithTeamAndProject(userId).orElse(null);
        log.info("user: {}", user);
        log.info("user team: {}", user.getTeam());
        log.info("user project: {}", user.getProject());
        return UserDto.Response.fromUser(user);
    }

    public List<UserDto.Response> getUserList() {
        List<User> userList = this.userRepository.findAll();
        return userList.stream().map(UserDto.Response::fromUser).toList();
    }


    public UserDto.Response updateUserInfo(Integer userId, String nickname, String teamName, String projectName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Team team = teamRepository.findTeamByName(teamName)
            .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        Project project = projectRepository.findProjectByName(projectName)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        user.setName(nickname);
        user.setTeam(team);
        user.setProject(project);
        User savedUser = userRepository.save(user);
        return UserDto.Response.fromUser(savedUser);
    }

    public String refreshAccessToken(String refreshToken) {
        return tokenProvider.reissueAccessToken(refreshToken);
    }

    public User signUp(String registrationId, Map<String, Object> oAuth2UserAttributes) {
        // 유저 정보 dto 생성
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);
        // 회원가입 및 로그인
        return getOrSave(oAuth2UserInfo);
    }

    private User getOrSave(OAuth2UserInfo oAuth2UserInfo) {
        User member = userRepository.findByEmail(oAuth2UserInfo.email())
            .orElseGet(oAuth2UserInfo::toEntity);
        member.setName(oAuth2UserInfo.name()); // for update name
        member.setSub(oAuth2UserInfo.sub()); // for update name
        return userRepository.save(member);
    }
}
