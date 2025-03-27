package io.hhplus.tdd.point;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class ConcurrentTest {

    @Autowired
    private PointService pointService;

    @Test
    void 동시성_문제_발생_테스트_charge() throws InterruptedException {
        //given
        long id = 1L;
        long amount = 100L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.charge(id, amount);
                } catch (Exception e) {

                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        UserPoint result = pointService.selectById(id);
        Assertions.assertThat(result.point()).isEqualTo(amount * threadCount);

        executorService.shutdown();

    }

    @Test
    void 동시성_문제_발생_테스트_use() throws InterruptedException {
        //given
        long id = 2L;
        long ownPoint = 10000L;
        long usePoint = 100L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        //when
        pointService.charge(id, ownPoint);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                   pointService.use(id, usePoint);
                } catch (Exception e) {

                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        UserPoint result = pointService.selectById(id);
        Assertions.assertThat(result.point()).isEqualTo(ownPoint - (usePoint * threadCount));
    }
}
