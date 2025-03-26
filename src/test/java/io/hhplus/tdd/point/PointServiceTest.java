package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;


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

    @Test
    void 포인트_사용_및_조회() {
        //given
        long id = 3L;
        long beforPoint = 1000L;
        long afterPoint = 100L;
        UserPoint beforUserPoint = pointService.charge(id, beforPoint);

        //when
        UserPoint afterUserPoint = pointService.use(beforUserPoint.id(), afterPoint);

        //then
        assertEquals(afterUserPoint.point(), beforPoint - afterPoint);
    }

    @Test
    void 포인트_히스토리_조회() {
        //given
        long id = 4L;
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

    @Test
    void 포인트_0이하_충전_및_사용() {
        //given
        long id = 5L;
        long chargedPoint1 = 0L;
        long chargedPoint2 = -10L;

        //when
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(id, chargedPoint1));
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(id, chargedPoint2));

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(id, chargedPoint1));
        IllegalArgumentException exception4 = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(id, chargedPoint2));

        //then
        assertEquals(exception1.getMessage(), "포인트는 1이상을 입력해 주세요.");
        assertEquals(exception2.getMessage(), "포인트는 1이상을 입력해 주세요.");

        assertEquals(exception3.getMessage(), "포인트는 1이상을 입력해 주세요.");
        assertEquals(exception4.getMessage(), "포인트는 1이상을 입력해 주세요.");
    }

    @Test
    void 포인트가_0보다_작아질때() {
        //given
        long id = 6L;
        long chargedPoint = 100L;
        long usePoint = 1000L;

        //when
        pointService.charge(id, chargedPoint);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.use(id, usePoint));

        //then
        assertEquals(exception.getMessage(), "포인트가 부족합니다.");
    }

    @Test
    void 최대_보유_포인트보다_클때() {
        //given
        long id = 7L;
        long ownPoint = 80000L;
        long chargedPoint = 30000L;

        //when
        pointService.charge(id, ownPoint);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pointService.charge(id, chargedPoint));

        //then
        assertEquals(exception.getMessage(), "최대 보유 가능한 포인트를 초과하였습니다.");
    }
}