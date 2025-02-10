package org.lgcns.bclayoutbe.service;


import static org.lgcns.bclayoutbe.exception.ErrorCode.CANNOT_FIND_LABEL;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lgcns.bclayoutbe.dto.LabelDto;
import org.lgcns.bclayoutbe.entity.Label;
import org.lgcns.bclayoutbe.entity.LabelUser;
import org.lgcns.bclayoutbe.entity.User;
import org.lgcns.bclayoutbe.exception.CustomException;
import org.lgcns.bclayoutbe.repository.LabelRepository;
import org.lgcns.bclayoutbe.repository.LabelUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelService {

    private final LabelRepository labelRepository;
    private final LabelUserRepository labelUserRepository;
    private final EntityManager entityManager;

    public List<LabelDto.Response> getLabelList(Integer userId) {
        List<Label> labelList = labelRepository.findAllByUserIdWithLabelUsersAndUsers(userId);
        return labelList.stream().map(LabelDto.Response::fromLabel).toList();
    }

    public LabelDto.Response getLabelInfo(Integer labelId) {
        List<LabelUser> labelUserList = labelUserRepository.findAllByLabelId(labelId);
        return LabelDto.Response.fromLabelUser(labelUserList);
    }

    @Transactional
    public LabelDto.Response updateLabel(Integer currentUserId, Integer labelId, String labelName,
        List<Integer> userIds) {
        // 1. Label 가져오거나 새로 생성
        log.info("### get label by label id : {}", labelId);
        Label label = getOrCreateLabel(currentUserId, labelId, labelName);

        // 2. 기존 LabelUser 목록 조회
        log.info("### get label user by label id : {}", labelId);
        Set<Integer> existingUserIds = getExistingUserIds(label.getLabelId());

        // 3. 추가할 LabelUser와 삭제할 LabelUser 찾기
        Set<Integer> newUserIds = new HashSet<>(userIds);
        List<LabelUser> usersToAdd = getUsersToAdd(label, newUserIds, existingUserIds);
        List<LabelUser> usersToRemove = getUsersToRemove(label.getLabelId(), newUserIds);

        // 4. DB 반영
        log.info("### delete label user");
        labelUserRepository.deleteAll(usersToRemove);
        log.info("### add label user");
        labelUserRepository.saveAll(usersToAdd);

        // 5. 최종 결과 반환
        return LabelDto.Response.fromLabelUser(labelUserRepository.findAllByLabelId(label.getLabelId()));
    }

    private Label getOrCreateLabel(Integer currentUserId, Integer labelId, String labelName) {
        if (labelId == null) {
            User userProxy = entityManager.getReference(User.class, currentUserId);
            Label newLabel = new Label();
            newLabel.setUser(userProxy);
            newLabel.setName(labelName);
            return labelRepository.save(newLabel);
        }
        return labelRepository.findById(labelId)
            .orElseThrow(() -> new CustomException(CANNOT_FIND_LABEL));
    }

    private Set<Integer> getExistingUserIds(Integer labelId) {
        return labelUserRepository.findAllByLabelId(labelId)
            .stream()
            .map(labelUser -> labelUser.getUser().getUserId())
            .collect(Collectors.toSet());
    }

    private List<LabelUser> getUsersToAdd(Label label, Set<Integer> newUserIds, Set<Integer> existingUserIds) {
        log.info("### try to make label user for add");
        return newUserIds.stream()
            .filter(userId -> !existingUserIds.contains(userId))
            .map(userId -> {
                User userProxy = entityManager.getReference(User.class, userId);
                LabelUser labelUser = new LabelUser();
                labelUser.setLabel(label);
                labelUser.setUser(userProxy);
                return labelUser;
            })
            .toList();
    }

    private List<LabelUser> getUsersToRemove(Integer labelId, Set<Integer> newUserIds) {
        log.info("### try to make label user for delete");
        return labelUserRepository.findAllByLabelId(labelId).stream()
            .filter(labelUser -> !newUserIds.contains(labelUser.getUser().getUserId()))
            .toList();
    }

}
