package top.kzre.krro.brush;

/**
 * 在线重采样装饰器：在两个连续事件之间按等间距插入中间点，
 * 保证 dab 分布均匀。间距由 SpacingFunction 决定。
 * 内部 Stroke 会收到所有原始事件以及插值生成的事件。
 */
final class ResampleStroke extends SmoothStroke {

    private final SpacingFunction spacingFn;
    private PointerEvent lastEvent = null;

    /**
     * @param inner      内部笔触
     * @param spacingFn  间距函数，返回两点间应保持的步长（像素）
     */
    public ResampleStroke(Stroke inner, SpacingFunction spacingFn) {
        super(inner);
        this.spacingFn = spacingFn;
    }

    @Override
    public Stroke push(PointerEvent e) {
        if (lastEvent != null) {
            float step = spacingFn.getStep(lastEvent, e);
            if (step > 0) {
                // 在 lastEvent 和 e 之间插值生成中间点（包含 e，不包含 lastEvent）
                interpolateAndPush(lastEvent, e, step);
            } else {
                // 步长为0，直接推送原事件
                inner.push(e);
            }
        } else {
            // 第一个点直接推送
            inner.push(e);
        }
        lastEvent = e;
        return this;
    }

    /**
     * 在 p1 和 p2 之间按 step 距离插入等距点，并将它们推入内部 Stroke。
     * 包含 p2，但不包含 p1。
     */
    private void interpolateAndPush(PointerEvent p1, PointerEvent p2, float step) {
        float dx = p2.getX() - p1.getX();
        float dy = p2.getY() - p1.getY();
        float dist = (float) Math.hypot(dx, dy);
        if (dist < 1e-6f) {
            inner.push(p2);
            return;
        }

        int segments = Math.max(1, (int) Math.floor(dist / step));

        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments; // 插值比例
            PointerEvent interpolated = PointerEvent.create(
                    lerp(p1.getX(), p2.getX(), t),
                    lerp(p1.getY(), p2.getY(), t),
                    lerp(p1.getPressure(), p2.getPressure(), t),
                    lerp(p1.getTiltX(), p2.getTiltX(), t),
                    lerp(p1.getTiltY(), p2.getTiltY(), t),
                    lerp(p1.getRotation(), p2.getRotation(), t),
                    p1.getTimestamp() + (long) ((p2.getTimestamp() - p1.getTimestamp()) * t),
                    lerp(p1.getPerspective(), p2.getPerspective(), t),
                    p2.getType()  // 通常使用当前事件的类型
            );
            inner.push(interpolated);
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public String toString() {
        return "ResampleStroke{" + inner + "}";
    }
}