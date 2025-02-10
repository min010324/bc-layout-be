package org.lgcns.bclayoutbe.repository;

import java.util.Optional;
import org.lgcns.bclayoutbe.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    Optional<Team> findTeamByName(String name);
}
