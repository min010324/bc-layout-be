package org.lgcns.bclayoutbe.scheduler;


import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lgcns.bclayoutbe.dto.SeatDto;
import org.lgcns.bclayoutbe.service.SeatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatScheduler {

    private final SeatService seatService;

    @Value("${SLACK_BOT_TOKEN}")
    private String SLACK_BOT_TOKEN;

    @Scheduled(cron = "0 0 5 * * *") // 매일 오전 5시
//    @Scheduled(cron = "0,30 * * * * *") // 매 0초, 30초 테스트용
    public void unusedSeatScheduler() throws SlackApiException, IOException {
        var seatMap = seatService.processUnusedSeat();
        Slack slack = Slack.getInstance();
        // Initialize an API Methods client with the given token
        MethodsClient methods = slack.methods(SLACK_BOT_TOKEN);
        for (Map.Entry<String, SeatDto.Response> entry : seatMap.entrySet()) {
            String sub = entry.getKey();
            SeatDto.Response seatResponse = entry.getValue();
            log.info("User Sub: {}", sub);
            log.info("Seat Info: {}", seatResponse.toString());
            // Build a request object
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(sub) // Use a channel ID `C1234567` is preferable
                .text("자리 이용이 마감되었습니다. 새로운 자리를 이용해주세요!")
                .build();

            // Get a response as a Java object
            ChatPostMessageResponse response = methods.chatPostMessage(request);
        }
    }

    @Scheduled(cron = "0 0 10 * * *") // 매일 오전 10시
//    @Scheduled(cron = "0,30 * * * * *") // 매 0초, 30초 테스트용
    public void notifyToSeatScheduler() throws SlackApiException, IOException {
        List<Integer> targetDayList = Arrays.asList(7, 3, 1);
        Slack slack = Slack.getInstance();
        // Initialize an API Methods client with the given token
        MethodsClient methods = slack.methods(SLACK_BOT_TOKEN);

        for (Integer targetDay : targetDayList) {
            var seatMap = seatService.processSeatsByNDayBefore(targetDay);
            for (Map.Entry<String, SeatDto.Response> entry : seatMap.entrySet()) {
                String sub = entry.getKey();
                SeatDto.Response seatResponse = entry.getValue();
                log.info("User Sub: {}", sub);
                log.info("Seat Info: {}", seatResponse.toString());
                // Build a request object
                ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(sub) // Use a channel ID `C1234567` is preferable
                    .text(String.format("자리 이용 마감 %d일 전입니다.", targetDay))
                    .build();

                // Get a response as a Java object
                ChatPostMessageResponse response = methods.chatPostMessage(request);
            }
        }
    }

}
