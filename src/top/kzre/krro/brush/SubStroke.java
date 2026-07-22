package top.kzre.krro.brush;

/**
 * 表示一个笔触的子段（视图）。
 * - 尾部视图（tail）：从原始笔触的当前末尾开始，支持 {@link #push} 动态扩展。
 * - 固定视图（fixed）：覆盖整个原始笔触，不支持 push。
 */
public final class SubStroke implements Stroke {
    private final Stroke source;
    private final boolean isTail;
    private final int startIndex;
    private final int fixedSize;   // 固定视图的大小，tail 视图忽略此字段

    private SubStroke(Stroke source, boolean isTail) {
        this.source = source;
        this.isTail = isTail;
        if (isTail) {
            this.startIndex = source.size();
            this.fixedSize = -1;          // 不使用
        } else {
            this.startIndex = 0;
            this.fixedSize = source.size();
        }
    }

    public static SubStroke createTail(Stroke source) {
        return new SubStroke(source, true);
    }

    public static SubStroke createFixed(Stroke source) {
        return new SubStroke(source, false);
    }

    @Override
    public Stroke push(PointerEvent e) {
        if (!isTail) {
            throw new UnsupportedOperationException("Fixed SubStroke does not support push");
        }
        source.push(e);
        return this;
    }

    @Override
    public int size() {
        return isTail ? source.size() - startIndex : fixedSize;
    }

    @Override
    public PointerEvent getEvent(int index) {
        return source.getEvent(startIndex + index);
    }

    @Override
    public PointerEvent getFirstEvent() {
        return size() > 0 ? getEvent(0) : null;
    }

    @Override
    public float getVelocity(PointerEvent event) {
        return source.getVelocity(event);  // 直接委托，因为事件引用唯一
    }

    @Override
    public float getCumulativeLength(PointerEvent event) {
        return source.getCumulativeLength(event);
    }

    @Override
    public String toString() {
        return String.format("SubStroke{range=[%d,%d), isTail=%s}", startIndex, startIndex + size(), isTail);
    }
}