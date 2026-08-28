package top.kzre.krro.brush;

/**
 * 在线重采样装饰器：在两个连续事件之间按等间距插入中间点，
 * 保证分布均匀。间距由 SpacingFunction 决定。
 * 内部 Stroke 会收到所有原始事件以及插值生成的事件。
 * 本类为不可变，每次 append 返回新实例。
 */
final class ResampleStroke extends SmoothStroke {

    private final SpacingFunction spacingFn;
    private final PointerEvent lastEvent; // 上一次处理的事件，用于计算间距

    private ResampleStroke(Stroke inner, SpacingFunction spacingFn, PointerEvent lastEvent) {
        super(inner);
        this.spacingFn = spacingFn;
        this.lastEvent = lastEvent;
    }

    /**
     * 创建新的重采样笔触，初始 lastEvent 为 null（表示尚未收到事件）。
     */
    static ResampleStroke create(Stroke inner, SpacingFunction spacingFn) {
        return new ResampleStroke(inner, spacingFn, null);
    }

    @Override
    public Stroke append(PointerEvent e) {
        Stroke newInner = inner;

        if (lastEvent != null) {
            float step = spacingFn.getStep(lastEvent, e);
            if (step > 0) {
                // 插值并返回更新后的内部笔触
                newInner = interpolateAndAppend(lastEvent, e, step);
            } else {
                // 无插值，直接追加当前事件
                newInner = newInner.append(e);
            }
        } else {
            // 首次事件，直接追加
            newInner = newInner.append(e);
        }

        // 返回新实例，lastEvent 更新为当前事件
        return new ResampleStroke(newInner, spacingFn, e);
    }

    /**
     * 在 p1 和 p2 之间按 step 距离插入等距点，并将它们追加到内部 Stroke。
     * 包含 p2，但不包含 p1。
     * 返回追加所有插值点后的新内部 Stroke。
     */
    private Stroke interpolateAndAppend(PointerEvent p1, PointerEvent p2, float step) {
        float dx = p2.getX() - p1.getX();
        float dy = p2.getY() - p1.getY();
        float dist = (float) Math.hypot(dx, dy);
        if (dist < 1e-6f) {
            return inner.append(p2);
        }

        int segments = Math.max(1, (int) Math.floor(dist / step));

        Stroke result = inner; // 当前内部笔触
        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            long interpolatedTimestamp = p1.getTimestamp() + (long) ((p2.getTimestamp() - p1.getTimestamp()) * t);

            PointerEvent interpolated = PointerEvent.newBuilder()
                    .x(lerp(p1.getX(), p2.getX(), t))
                    .y(lerp(p1.getY(), p2.getY(), t))
                    .pressure(lerp(p1.getPressure(), p2.getPressure(), t))
                    .tiltX(lerp(p1.getTiltX(), p2.getTiltX(), t))
                    .tiltY(lerp(p1.getTiltY(), p2.getTiltY(), t))
                    .rotation(lerp(p1.getRotation(), p2.getRotation(), t))
                    .timestamp(interpolatedTimestamp)
                    .type(p2.getType())
                    .build();

            result = result.append(interpolated);
        }
        return result;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public String toString() {
        return "ResampleStroke{" + inner + "}";
    }
}