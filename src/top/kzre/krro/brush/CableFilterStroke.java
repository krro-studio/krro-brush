package top.kzre.krro.brush;

import top.kzre.smooth.CompositeFilter2D;

final class CableFilterStroke extends SmoothStroke {

    private final CompositeFilter2D filter;

    private CableFilterStroke(Stroke inner, CompositeFilter2D filter) {
        super(inner);
        this.filter = filter;
    }

    static CableFilterStroke create(Stroke inner, double alpha, double initX, double initY) {
        CompositeFilter2D filter = CompositeFilter2D.newCable2D(alpha, initX, initY);
        return new CableFilterStroke(inner, filter);
    }

    @Override
    public Stroke append(PointerEvent e) {
        CompositeFilter2D newFilter = filter.filter(e.getX(), e.getY());
        double smoothX = newFilter.getX();   // 滤波后坐标
        double smoothY = newFilter.getY();

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

        Stroke newInner = inner.append(smoothed);
        return new CableFilterStroke(newInner, newFilter);
    }
}