package top.kzre.krro.brush;

import top.kzre.smooth.CableFilter2D;

/**
 * 基于 CableFilter2D 的在线平滑笔触。
 * 坐标经过低通滤波后传递给内部 Stroke，其他属性保持不变。
 */
final class CableFilterStroke extends SmoothStroke {

    private final CableFilter2D filter;
    private final CableFilter2D.CableState filterState;

    public CableFilterStroke(Stroke inner, CableFilter2D filter, CableFilter2D.CableState filterState) {
        super(inner);
        this.filter = filter;
        this.filterState = filterState;
    }

    @Override
    public Stroke push(PointerEvent e) {
        filter.filter(e.getX(), e.getY(), filterState);
        double smoothX = filterState.getFilteredX();
        double smoothY = filterState.getFilteredY();

        PointerEvent smoothed = PointerEvent.newBuilder()
                .x((float) smoothX)
                .y((float) smoothY)
                .pressure(e.getPressure())
                .tiltX(e.getTiltX())
                .tiltY(e.getTiltY())
                .rotation(e.getRotation())
                .timestamp(e.getTimestamp())
                .type(e.getType())
                .build();

        inner.push(smoothed);
        return this;
    }
}