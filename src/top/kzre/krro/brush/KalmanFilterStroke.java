package top.kzre.krro.brush;

import top.kzre.smooth.CompositeFilter2D;

final class KalmanFilterStroke extends SmoothStroke {

    private final CompositeFilter2D filter;

    private KalmanFilterStroke(Stroke inner, CompositeFilter2D filter) {
        super(inner);
        this.filter = filter;
    }

    static KalmanFilterStroke create(Stroke inner,
                                     double processNoise,
                                     double measurementNoise,
                                     double initX,
                                     double initY,
                                     double initError) {
        CompositeFilter2D filter = CompositeFilter2D.newKalman2D(processNoise, measurementNoise,
                initX, initY, initError);
        return new KalmanFilterStroke(inner, filter);
    }

    @Override
    public Stroke append(PointerEvent e) {
        CompositeFilter2D newFilter = filter.filter(e.getX(), e.getY());
        double smoothX = newFilter.getX();
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
        return new KalmanFilterStroke(newInner, newFilter);
    }
}