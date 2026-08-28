package top.kzre.krro.brush;

import clojure.lang.IPersistentVector;

public abstract class StrokeDecorator extends AbstractStroke implements Stroke {
    protected final Stroke inner;

    protected StrokeDecorator(Stroke inner) {
        super(inner.getBrushSpec());
        this.inner = inner;
    }

    @Override
    public IPersistentVector getStroke() {
        return inner.getStroke();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public PointerEvent getEvent(int index) {
        return inner.getEvent(index);
    }

    @Override
    public PointerEvent getFirstEvent() {
        return inner.getFirstEvent();
    }

    @Override
    public float getVelocity(PointerEvent event) { return inner.getVelocity(event); }

    @Override
    public float getCumulativeLength(PointerEvent event) {
        return inner.getCumulativeLength(event);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + inner + "}";
    }
}

