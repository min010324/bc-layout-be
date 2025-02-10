package org.lgcns.bclayoutbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "seat_history")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SeatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "seat_id")
    private Integer seatId;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @UpdateTimestamp
    @Column(name = "end_date")
    private LocalDateTime endDate;
}
