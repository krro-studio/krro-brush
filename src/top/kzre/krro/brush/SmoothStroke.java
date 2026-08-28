package top.kzre.krro.brush;

import top.kzre.smooth.CompositeFilter2D;

/**
 * 平滑笔触抽象基类（模板模式），子类为不可变装饰器。
 * 提供创建常见平滑笔触的静态工厂方法，所有方法返回不可变实例。
 */
public abstract class SmoothStroke extends StrokeDecorator {

    protected SmoothStroke(Stroke inner) {
        super(inner);
    }

    /**
     * 创建 Cable 低通滤波笔触。
     * 初始坐标取自内部笔触的第一个事件，若内部为空则使用 (0,0)。
     */
    public static SmoothStroke cable(Stroke inner, double alpha) {
        double initX = 0, initY = 0;
        if (!inner.isEmpty()) {
            PointerEvent first = inner.getFirstEvent();
            initX = first.getX();
            initY = first.getY();
        }
        return CableFilterStroke.create(inner, alpha, initX, initY);
    }

    /**
     * 创建卡尔曼滤波笔触。
     * @param processNoise     过程噪声方差
     * @param measurementNoise 测量噪声方差
     * @param initError        初始协方差（对角线值）
     */
    public static SmoothStroke kalman(Stroke inner,
                                      double processNoise,
                                      double measurementNoise,
                                      double initError) {
        double initX = 0, initY = 0;
        if (!inner.isEmpty()) {
            PointerEvent first = inner.getFirstEvent();
            initX = first.getX();
            initY = first.getY();
        }
        return KalmanFilterStroke.create(inner, processNoise, measurementNoise,
                initX, initY, initError);
    }

    /**
     * 重采样笔触（保持不可变，需自行实现）。
     */
    public static SmoothStroke resample(Stroke inner, SpacingFunction spacingFn) {
        return ResampleStroke.create(inner, spacingFn);
    }
}