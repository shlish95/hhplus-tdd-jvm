package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceUnitTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @DisplayName("포인트 충전 정상 처리 및 히스토리 입력 확인")
    @Test
    void charge() {
        //given
        long id = 1L;
        long ownPoint = 1000L;
        long addPoint = 100L;

        UserPoint originUserPoint = new UserPoint(id, ownPoint, System.currentTimeMillis());
        UserPoint afterAddUserPoint = new UserPoint(id, ownPoint + addPoint, System.currentTimeMillis());

        when(userPointTable.selectById(id)).thenReturn(originUserPoint);
        when(userPointTable.insertOrUpdate(id, ownPoint + addPoint)).thenReturn(afterAddUserPoint);

        //when
        UserPoint userPoint = pointService.charge(id, addPoint);

        //then
        assertThat(userPoint).isEqualTo(afterAddUserPoint);
        assertThat(userPoint.point()).isEqualTo(ownPoint + addPoint);
        verify(pointHistoryTable).insert(id, ownPoint + addPoint, TransactionType.CHARGE, userPoint.updateMillis());
    }

    @DisplayName("포인트 사용 정상 처리 및 히스토리 입력 확인")
    @Test
    void use() {
        //given
        long id = 2L;
        long ownPoint = 1000L;
        long usePoint = 100L;

        UserPoint originUserPoint = new UserPoint(id, ownPoint, System.currentTimeMillis());
        UserPoint afterUseUserPoint = new UserPoint(id, ownPoint - usePoint, System.currentTimeMillis());

        when(userPointTable.selectById(id)).thenReturn(originUserPoint);
        when(userPointTable.insertOrUpdate(id, ownPoint - usePoint)).thenReturn(afterUseUserPoint);

        //when
        UserPoint userPoint = pointService.use(id, usePoint);

        //then
        assertThat(userPoint).isEqualTo(afterUseUserPoint);
        assertThat(userPoint.point()).isEqualTo(ownPoint - usePoint);
        verify(pointHistoryTable).insert(id, ownPoint - usePoint, TransactionType.USE, userPoint.updateMillis());
    }

    @Nested
    class validateTest {
        @Test
        void 포인트_0보다_작은값_입력_예외() {
            //given
            UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());

            //when //then
            assertThatCode(() -> pointService.pointValidate(userPoint, 1L, TransactionType.USE))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> pointService.pointValidate(userPoint, 0L, TransactionType.CHARGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("포인트는 1이상을 입력해 주세요.");
            assertThatThrownBy(() -> pointService.pointValidate(userPoint, -1L, TransactionType.USE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("포인트는 1이상을 입력해 주세요.");
        }

        @Test
        void 포인트_부족_예외() {
            //given
            UserPoint userPoint = new UserPoint(2L, 100L, System.currentTimeMillis());

            //when //then
            assertThatCode(() -> pointService.pointValidate(userPoint, 100L, TransactionType.USE))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> pointService.pointValidate(userPoint, 101L, TransactionType.USE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("포인트가 부족합니다.");
        }

        @Test
        void 포인트_초과_예외() {
            //given
            UserPoint userPoint = new UserPoint(3L, 0L, System.currentTimeMillis());

            //when //then
            assertThatCode(() -> pointService.pointValidate(userPoint, 99999L, TransactionType.CHARGE))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> pointService.pointValidate(userPoint, 99999L + 1L, TransactionType.CHARGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("최대 보유 가능한 포인트를 초과하였습니다.");
        }
    }

}
