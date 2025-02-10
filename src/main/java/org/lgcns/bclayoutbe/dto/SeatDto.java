package org.lgcns.bclayoutbe.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.lgcns.bclayoutbe.entity.Seat;

@Getter
@Builder
@AllArgsConstructor
public class SeatDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @ToString
    public static class Response {

        private Integer seatId;
        private String useYn;
        private LocalDateTime endDate;
        private UserDto.Response user;

        public static SeatDto.Response fromSeat(final Seat seat) {
            return SeatDto.Response.builder()
                .seatId(seat.getSeatId())
                .useYn(seat.getUseYn())
                .endDate(seat.getEndDate())
                .user(UserDto.Response.fromUser(seat.getUser()))
                .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Request {

        private Integer seatId;
        private String useYn;
        //        private LocalDateTime endDate;
        private LocalDate endDate;
    }

}
