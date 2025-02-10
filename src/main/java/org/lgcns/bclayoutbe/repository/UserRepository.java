package org.lgcns.bclayoutbe.repository;

import java.util.Optional;
import org.lgcns.bclayoutbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.team LEFT JOIN FETCH u.project WHERE u.userId = :userId")
    Optional<User> findByIdWithTeamAndProject(Integer userId);

    Optional<User> findByEmail(String email);
}
