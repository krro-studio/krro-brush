package top.kzre.krro.brush;

import top.kzre.smooth.CableFilter2D;
import top.kzre.smooth.KalmanFilter2D;

/**
 * 平滑笔触抽象基类（模板模式）。
 * 子类只需实现 {@link #push(PointerEvent)} 来定义平滑逻辑，
 * 其余查询方法自动委托给内部 {@code inner} Stroke。
 * 同时作为工厂，提供创建常见平滑笔触的静态方法。
 */
public abstract class SmoothStroke implements Stroke {

    protected final Stroke inner;

    protected SmoothStroke(Stroke inner) {
        this.inner = inner;
    }

    /**
     * 工厂方法：创建使用 CableFilter2D 的平滑笔触。
     *
     * @param inner 被装饰的原始笔触
     * @param alpha 平滑系数 (0~1)
     */
    public static SmoothStroke cable(Stroke inner, float alpha) {
        CableFilter2D cable = CableFilter2D.instance();
        CableFilter2D.CableState state = (CableFilter2D.CableState) CableFilter2D.newState(alpha);
        return new CableFilterStroke(inner, cable, state);
    }

    /**
     * 创建卡尔曼平滑笔触。
     *
     * @param inner            内部笔触
     * @param processNoise     过程噪声方差（例如 0.01）
     * @param measurementNoise 测量噪声方差（例如 0.1）
     * @param initX, initY    初始位置（通常为第一个事件的坐标）
     */
    public static SmoothStroke kalman(Stroke inner,
                                      double processNoise,
                                      double measurementNoise,
                                      double initX,
                                      double initY) {
        KalmanFilter2D kf = KalmanFilter2D.instance();
        KalmanFilter2D.State state = (KalmanFilter2D.State) KalmanFilter2D.newState(
                processNoise, measurementNoise, initX, initY, 0.0, 0.0, 1.0);
        return new KalmanFilterStroke(inner, kf, state);
    }

    public static SmoothStroke resample(Stroke inner, SpacingFunction spacingFn) {
        return new ResampleStroke(inner, spacingFn);
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