package top.kzre.krro.brush;

/**
 * 表示一个笔触的子段（视图）。
 * - 尾部视图（tail）：从原始笔触的当前末尾开始，支持 {@link #push} 动态扩展。
 * - 固定视图（fixed）：覆盖原始笔触的一段，不支持 push。
 * 所有查询方法均相对于子段索引，速度和累积长度自动委托给源 Stroke。
 */
public final class SubStroke implements Stroke {
    private final Stroke source;
    private final boolean isTail;
    private final int startIndex;
    private final int fixedSize;   // 固定视图的大小，tail 视图忽略此字段

    /** 通用构造器，支持所有类型的子段 */
    private SubStroke(Stroke source, int startIndex, int fixedSize, boolean isTail) {
        this.source = source;
        this.startIndex = startIndex;
        this.fixedSize = fixedSize;
        this.isTail = isTail;
    }

    /** 创建一个尾部视图，起始于原始笔触的当前末尾，支持 push 动态扩展 */
    public static SubStroke createTail(Stroke source) {
        return new SubStroke(source, source.size(), -1, true);
    }

    /** 创建一个固定视图，覆盖从 startIndex 开始、长度为 size 的区间 */
    public static SubStroke createFixed(Stroke source, int startIndex, int size) {
        if (startIndex < 0 || size < 0 || startIndex + size > source.size()) {
            throw new IndexOutOfBoundsException("Invalid range for fixed SubStroke");
        }
        return new SubStroke(source, startIndex, size, false);
    }

    /** 创建一个固定视图，覆盖整个原始笔触 */
    public static SubStroke createFixed(Stroke source) {
        return createFixed(source, 0, source.size());
    }

    /** 创建一个固定视图，从原始笔触末尾往前截取指定数量的事件 */
    public static SubStroke createFixedFromEnd(Stroke source, int count) {
        int total = source.size();
        int size = Math.min(count, total);
        int start = total - size;
        return createFixed(source, start, size);
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
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return source.getEvent(startIndex + index);
    }

    @Override
    public PointerEvent getFirstEvent() {
        return size() > 0 ? getEvent(0) : null;
    }

    @Override
    public float getVelocity(PointerEvent event) {
        return source.getVelocity(event);
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