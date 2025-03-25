package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;


    @DisplayName("포인트 충전 및 조회")
    @Test
    void 포인트_충전_및_조회() {
        //given
        long firstId = 1L;
        long secondId = 2L;
        long firstPoint = 1000L;
        long secondPoint = 1000L;

        //when
        UserPoint userPointA = pointService.charge(firstId, firstPoint);
        UserPoint userPointB = pointService.charge(secondId, firstPoint + secondPoint);

        //then
        assertEquals(userPointA.point(), firstPoint);
        assertEquals(userPointB.point(), firstPoint + secondPoint);
    }

    @DisplayName("포인트 사용 및 조회")
    @Test
    void 포인트_사용_및_조회() {
        //given
        long id = 1L;
        long beforPoint = 10000L;
        long afterPoint = 1000L;
        UserPoint beforUserPoint = pointService.charge(id, beforPoint);

        //when
        UserPoint afterUserPoint = pointService.use(beforUserPoint.id(), afterPoint);

        //then
        assertEquals(afterUserPoint.point(), beforPoint - afterPoint);
    }

    @DisplayName("포인트 히스토리 조회")
    @Test
    void 포인트_히스토리_조회() {
        //given
        long id = 1L;
        long chargedPoint = 1000L;
        long usePoint = 100L;

        UserPoint userPoint = pointService.charge(id, chargedPoint);
        pointService.use(userPoint.id(), usePoint);

        //when
        List<PointHistory> history = pointService.history(userPoint.id());

        //then
        assertEquals(history.size(), 2);
        assertEquals(chargedPoint, history.get(0).amount());
        assertEquals(chargedPoint - usePoint, history.get(1).amount());
        assertEquals(history.get(0).type(), TransactionType.CHARGE);
        assertEquals(history.get(1).type(), TransactionType.USE);
    }
}