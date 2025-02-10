package org.lgcns.bclayoutbe.repository;

import java.util.Optional;
import org.lgcns.bclayoutbe.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    Optional<Project> findProjectByName(String name);

}
