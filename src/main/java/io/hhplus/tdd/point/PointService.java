package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final Lock lock = new ReentrantLock();

    public UserPoint selectById(Long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint charge(Long id, Long amount) {
        lock.lock();

        try {
            UserPoint userPoint = selectById(id);
            pointValidate(userPoint, amount, TransactionType.CHARGE);

            long currentPoint = userPoint.point() + amount;

            pointHistoryTable.insert(id, currentPoint, TransactionType.CHARGE, userPoint.updateMillis());

            return userPointTable.insertOrUpdate(userPoint.id(), currentPoint);
        } finally {
            lock.unlock();
        }

    }

    public UserPoint use(Long id, Long amount) {
        lock.lock();
        try {
            UserPoint userPoint = selectById(id);
            pointValidate(userPoint, amount, TransactionType.USE);

            long currentPoint = userPoint.point() - amount;

            pointHistoryTable.insert(id, currentPoint, TransactionType.USE, userPoint.updateMillis());

            return userPointTable.insertOrUpdate(userPoint.id(), currentPoint);
        } finally {
            lock.unlock();
        }
    }

    public List<PointHistory> history(Long id) {
        List<PointHistory> pointHistory = pointHistoryTable.selectAllByUserId(id);

        return pointHistory;
    }

    public void pointValidate(UserPoint userPoint, Long amount, TransactionType transactionType) {
        if (0 >= amount) {
            throw new IllegalArgumentException("포인트는 1이상을 입력해 주세요.");
        }

        if (transactionType == TransactionType.USE) {
            userPoint.checkLessThanZero(userPoint, amount);
        } else if (transactionType == TransactionType.CHARGE) {
            userPoint.checkGreaterThanMax(userPoint, amount);
        }
    }

}
