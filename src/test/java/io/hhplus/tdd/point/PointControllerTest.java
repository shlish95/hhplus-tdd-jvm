package io.hhplus.tdd.point;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;


    @Test
    void 특정_유저_포인트_조회() throws Exception {
        //given
        long id = 1L;
        long amount = 1000L;

        //when
        pointService.charge(id, amount);

        //then
        mockMvc.perform(get("/point/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(amount));
    }

    @Test
    void 특정_유저_포인트_충전() throws Exception {
        //given
        long id = 2L;
        long amount = 1000L;
        long chargePoint = 100L;

        //when
        pointService.charge(id, amount);

        //then
        mockMvc.perform(patch("/point/{id}/charge", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(chargePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(amount + chargePoint));
    }

    @Test
    void 특정_유저_포인트_충전_초과() throws Exception {
        //given
        long id = 3L;
        long hugePoint = 100000L;

        //when //then
        mockMvc.perform(patch("/point/{id}/charge", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(hugePoint)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."));
    }

    @Test
    void 특정_유저_포인트_사용() throws Exception {
        //given
        long id = 4L;
        long ownPoint = 1000L;
        long usePoint = 100L;

        //when
        pointService.charge(id, ownPoint);

        //then
        mockMvc.perform(patch("/point/{id}/use", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(usePoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(ownPoint - usePoint));
    }

    @Test
    void 특정_유저_포인트_사용_미만() throws Exception {
        //given
        long id = 5L;
        long onwPoint = 999L;
        long usePoint = 1000L;

        //when
        pointService.charge(id, onwPoint);

        //then
        mockMvc.perform(patch("/point/{id}/use", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(usePoint)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("에러가 발생했습니다."));
    }

    @Test
    void 특정_유저_포인트_히스토리() throws Exception {
        //given
        long id = 6L;
        long chargedPoint = 1000L;
        long usePoint = 200L;

        //when
        pointService.charge(id, chargedPoint);
        pointService.use(id, usePoint);

        //then
        mockMvc.perform(get("/point/{id}/histories", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(chargedPoint))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].amount").value(chargedPoint - usePoint))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }

    @Test
    void 동시성_테스트_특정_유저_포인트_충전() throws Exception {
        //given
        long id = 7L;
        long chargedPoint = 100L;
        int treadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(treadCount);
        CountDownLatch countDownLatch = new CountDownLatch(treadCount);

        //when
        for (int i = 0; i < treadCount; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(patch("/point/{id}/charge", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.valueOf(chargedPoint)))
                            .andExpect(status().isOk());
                } catch (Exception e) {

                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //then
        mockMvc.perform(get("/point/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(chargedPoint * treadCount));
    }

    @Test
    void 동시성_테스트_특정_유저_포인트_사용() throws Exception {
        //given
        long id = 8L;
        long ownPoint = 1000L;
        long usePoint = 20L;
        int treadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(treadCount);
        CountDownLatch countDownLatch = new CountDownLatch(treadCount);

        //when
        pointService.charge(id, ownPoint);

        for (int i = 0; i < treadCount; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(patch("/point/{id}/use", id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(String.valueOf(usePoint)))
                            .andExpect(status().isOk());
                } catch (Exception e) {

                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //then
        mockMvc.perform(get("/point/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(ownPoint - (usePoint * treadCount)));
    }
}