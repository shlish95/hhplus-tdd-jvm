package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    private static final long maxPoint = 99999L;

    public void checkLessThanZero(UserPoint userPoint, long point) {
        long resultPoint = userPoint.point() - point;

        if (resultPoint < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }

    public void checkGreaterThanMax(UserPoint userPoint, long point) {
        long resultPoint = userPoint.point() + point;

        if (resultPoint > maxPoint) {
            throw new IllegalArgumentException("최대 보유 가능한 포인트를 넘었습니다.");
        }
    }

}
