package top.kzre.krro.brush;

/**
 * 计算两点之间的目标步长（像素）。
 * 可实现自适应间距，例如根据压力或速度动态调整。
 */
@FunctionalInterface
public interface SpacingFunction {
    float getStep(PointerEvent prev, PointerEvent current);
}