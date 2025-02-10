package org.lgcns.bclayoutbe.repository;

import java.util.List;
import org.lgcns.bclayoutbe.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LabelRepository extends JpaRepository<Label, Integer> {

    @Query("SELECT l FROM Label l WHERE l.user.userId = :userId")
    List<Label> findAllByUserId(Integer userId);

    @Query("""
            SELECT l FROM Label l
            LEFT JOIN FETCH l.labelUserList lu
            LEFT JOIN FETCH lu.user
            WHERE l.user.userId = :userId
        """)
    List<Label> findAllByUserIdWithLabelUsersAndUsers(Integer userId);
}
