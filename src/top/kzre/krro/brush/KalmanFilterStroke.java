package top.kzre.krro.brush;

import top.kzre.smooth.KalmanFilter2D;

/**
 * 基于卡尔曼滤波的在线平滑笔触。
 * 利用速度预测，动态响应更快，适合快速涂抹或书法笔触。
 */
final class KalmanFilterStroke extends SmoothStroke {

    private final KalmanFilter2D filter;
    private final KalmanFilter2D.State filterState;

    public KalmanFilterStroke(Stroke inner, KalmanFilter2D filter, KalmanFilter2D.State filterState) {
        super(inner);
        this.filter = filter;
        this.filterState = filterState;
    }


    @Override
    public Stroke push(PointerEvent e) {
        // 用滤波器更新位置
        filter.filter(e.getX(), e.getY(), filterState);
        double smoothX = filterState.getFilteredX();
        double smoothY = filterState.getFilteredY();

        PointerEvent smoothed = PointerEvent.create(
                (float) smoothX,
                (float) smoothY,
                e.getPressure(),
                e.getTiltX(),
                e.getTiltY(),
                e.getRotation(),
                e.getTimestamp(),
                e.getPerspective(),
                e.getType()
        );
        inner.push(smoothed);
        return this;
    }
}