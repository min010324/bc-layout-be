package org.lgcns.bclayoutbe.service;


import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.lgcns.bclayoutbe.dto.SeatDto;
import org.lgcns.bclayoutbe.entity.Seat;
import org.lgcns.bclayoutbe.entity.SeatHistory;
import org.lgcns.bclayoutbe.entity.User;
import org.lgcns.bclayoutbe.exception.CustomException;
import org.lgcns.bclayoutbe.exception.ErrorCode;
import org.lgcns.bclayoutbe.repository.SeatHistoryRepository;
import org.lgcns.bclayoutbe.repository.SeatRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatHistoryRepository seatHistoryRepository;
    private final EntityManager entityManager;

    public ArrayList<SeatDto.Response> getAllSeat() {
        var seatList = this.seatRepository.findAllWithUser();
        return seatList.stream()
            .map(SeatDto.Response::fromSeat)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public SeatDto.Response updateSeatInfo(Integer seatId, Integer userId, String usedYn, LocalDateTime endDate) {
        Seat seatInfo = this.seatRepository.findById(seatId).orElseThrow(IllegalArgumentException::new);
        if ("N".equals(usedYn)) {
            seatInfo.setUser(null);
            seatInfo.setUseYn("N");
        } else {
            seatInfo.setUseYn("Y");
            seatInfo.setEndDate(endDate);
            seatInfo.setStartDate(LocalDateTime.now());
            // userId를 User 객체 대신 프록시로 설정하여 불필요한 SELECT 방지
            User userProxy = this.entityManager.getReference(User.class, userId);
            seatInfo.setUser(userProxy);
        }
        try {
            Seat savedSeat = this.seatRepository.save(seatInfo);
            SeatHistory history = SeatHistory.builder()
                .userId(userId)
                .seatId(savedSeat.getSeatId())
                .useYn(savedSeat.getUseYn())
                .endDate(savedSeat.getEndDate())
                .startDate(savedSeat.getStartDate()).build();
            this.seatHistoryRepository.save(history);
            return SeatDto.Response.fromSeat(savedSeat);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_USE_SEAT);
        }
    }

    @Transactional
    public Map<String, SeatDto.Response> processUnusedSeat() {
        var seatList = this.seatRepository.findAllWithUser();
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기

        List<Seat> targetSeats = seatList.stream() // endDate가 오늘 이전인 경우만 필터링
            .filter(seat -> seat.getEndDate() != null && "Y".equals(seat.getUseYn()) && seat.getEndDate().toLocalDate()
                .isBefore(today))
            .toList();
        // User의 sub 값을 key로 Map 생성
        var target = targetSeats.stream()
            .collect(Collectors.toMap(seat -> seat.getUser().getSub(), SeatDto.Response::fromSeat));

        // 사용 종료된 좌석 정보 업데이트
        targetSeats.forEach(seat -> {
            seat.setUseYn("N");
            seat.setUser(null);
        });

        // DB에 반영
        this.seatRepository.saveAll(targetSeats);  // todo batch update로 개선

        return target;
    }

    public Map<String, SeatDto.Response> processSeatsByNDayBefore(Integer day) {
        var seatList = this.seatRepository.findAllWithUser();
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기

        // endDate가 각각 7일 후, 3일 후, 1일 후인 좌석들 필터링
        List<Seat> targetSeats = seatList.stream()
            .filter(seat -> seat.getEndDate() != null && "Y".equals(seat.getUseYn()) && seat.getEndDate().toLocalDate()
                .isEqual(today.plusDays(day)))
            .toList();
        // 결과를 Map으로 반환
        return targetSeats.stream()
            .collect(Collectors.toMap(seat -> seat.getUser().getSub(), SeatDto.Response::fromSeat));

    }
}
