package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;

/**
 * 一个不可变的 Stroke 视图，表示父 Stroke 的子序列。
 * 不支持 append 操作。
 * 所有事件的速度、累积长度和笔触起始事件保持父 Stroke 的全局值。
 */
public final class StrokeView implements Stroke {

    private final Stroke parent;
    private final int start;
    private final int length;

    /**
     * 构造一个 SubStroke 视图。
     *
     * @param parent 父 Stroke，不能为 null
     * @param start  起始索引（包含），必须 >= 0
     * @param length 子序列长度，必须 >= 0 且 start + length <= parent.size()
     */
    public StrokeView(Stroke parent, int start, int length) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        if (start < 0) {
            throw new IllegalArgumentException("start must be >= 0");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }
        if (start + length > parent.size()) {
            throw new IllegalArgumentException("subrange out of bounds");
        }
        this.parent = parent;
        this.start = start;
        this.length = length;
    }

    /**
     * 创建一个包含末尾最多 n 个事件的 SubStroke 视图。
     */
    public static StrokeView last(Stroke parent, int n) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        int total = parent.size();
        if (n <= 0) {
            return new StrokeView(parent, total, 0);
        }
        if (n >= total) {
            return new StrokeView(parent, 0, total);
        }
        return new StrokeView(parent, total - n, n);
    }

    /**
     * 创建一个包含开头最多 n 个事件的 SubStroke 视图。
     */
    public static StrokeView first(Stroke parent, int n) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        int total = parent.size();
        if (n <= 0) {
            return new StrokeView(parent, 0, 0);
        }
        int count = Math.min(n, total);
        return new StrokeView(parent, 0, count);
    }

    @Override
    public Stroke append(PointerEvent e) {
        throw new UnsupportedOperationException("append is not supported for SubStroke");
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public PointerEvent getEvent(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + length);
        }
        return parent.getEvent(start + index);
    }

    @Override
    public PointerEvent getFirstEvent() {
        // 返回父 Stroke 的第一个事件（全局属性）
        return parent.getFirstEvent();
    }

    @Override
    public float getVelocity(PointerEvent event) {
        // 速度是全局属性
        return parent.getVelocity(event);
    }

    @Override
    public float getCumulativeLength(PointerEvent event) {
        // 累积长度是全局属性
        return parent.getCumulativeLength(event);
    }

    @Override
    public IPersistentVector getStroke() {
        IPersistentVector full = parent.getStroke();
        return RT.subvec(full, start, start + length);
    }

    @Override
    public IPersistentMap getBrushSpec() {
        return parent.getBrushSpec();
    }
}