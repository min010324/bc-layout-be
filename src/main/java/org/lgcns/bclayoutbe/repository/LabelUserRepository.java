package org.lgcns.bclayoutbe.repository;

import java.util.List;
import org.lgcns.bclayoutbe.entity.LabelUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LabelUserRepository extends JpaRepository<LabelUser, Integer> {

    @Query("SELECT lu FROM LabelUser lu left join fetch lu.label left join fetch lu.user WHERE lu.label.labelId = :labelId")
    List<LabelUser> findAllByLabelId(Integer labelId);

}
