package org.lgcns.bclayoutbe.repository;

import java.util.ArrayList;
import java.util.Optional;
import org.lgcns.bclayoutbe.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

    @Query("SELECT s FROM Seat s LEFT JOIN FETCH s.user u LEFT JOIN FETCH u.team LEFT JOIN FETCH u.project")
    ArrayList<Seat> findAllWithUser();

    @Query("SELECT s FROM Seat s LEFT JOIN FETCH s.user WHERE s.seatId = :seatId")
    Optional<Seat> findByIdWithUser(Integer seatId);
}
