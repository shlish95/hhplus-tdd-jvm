package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint selectById(Long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint charge(Long id, Long amount) {
        UserPoint userPoint = selectById(id);
        long currentPoint = userPoint.point() + amount;

        pointHistoryTable.insert(id, currentPoint, TransactionType.CHARGE, userPoint.updateMillis());

        return userPointTable.insertOrUpdate(userPoint.id(), currentPoint);
    }

    public UserPoint use(Long id, Long amount) {
        UserPoint userPoint = selectById(id);
        long currentPoint = userPoint.point() - amount;

        pointHistoryTable.insert(id, currentPoint, TransactionType.USE, userPoint.updateMillis());

        return userPointTable.insertOrUpdate(userPoint.id(), currentPoint);
    }

    public List<PointHistory> history(Long id) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);

        return pointHistories;
    }
}
