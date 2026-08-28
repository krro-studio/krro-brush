package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;
import java.util.ArrayList;
import java.util.IdentityHashMap;

public final class DefaultStroke extends AbstractStroke {
    private final ArrayList<DefaultStroke> strokes;           // 所有版本共享的事件节点列表
    private final IdentityHashMap<PointerEvent, Integer> indexMap; // 共享的引用→索引映射
    final PointerEvent lastEvent;     // 当前节点对应的事件（空节点为 null）
    final float velocity;             // 当前事件相对前一事件的速度
    final float cumulativeLength;     // 到当前事件为止的累积路径长度
    final int index;                  // 当前节点在 strokes 列表中的索引，空节点为 -1

    private static final int INITIAL_CAPACITY = 128;

    // 私有构造器：创建新节点（包括空节点）
    private DefaultStroke(int index,
                          IPersistentMap brushSpec,
                          ArrayList<DefaultStroke> strokes,
                          IdentityHashMap<PointerEvent, Integer> indexMap,
                          PointerEvent lastEvent,
                          float velocity,
                          float cumulativeLength) {
        super(brushSpec);
        this.index = index;
        this.strokes = strokes;
        this.indexMap = indexMap;
        this.lastEvent = lastEvent;
        this.velocity = velocity;
        this.cumulativeLength = cumulativeLength;
    }

    // 工厂方法：创建空笔触
    public static DefaultStroke create(IPersistentMap brushSpec) {
        ArrayList<DefaultStroke> strokes = new ArrayList<>();
        IdentityHashMap<PointerEvent, Integer> indexMap = new IdentityHashMap<>(INITIAL_CAPACITY);
        return new DefaultStroke(-1, brushSpec, strokes, indexMap, null, 0f, 0f);
    }

    @Override
    public Stroke append(PointerEvent e) {
        float vel = 0f;
        float len = this.cumulativeLength; // 继承前一个累积长度

        if (lastEvent != null) {
            float dx = e.getX() - lastEvent.getX();
            float dy = e.getY() - lastEvent.getY();
            float dist = (float) Math.hypot(dx, dy);
            long dt = e.getTimestamp() - lastEvent.getTimestamp();
            if (dt > 0) {
                vel = dist / dt;
            }
            len += dist;
        }

        int newIndex = this.index + 1; // 新节点在共享列表中的索引
        DefaultStroke newStroke = new DefaultStroke(
                newIndex,
                getBrushSpec(),          // 从 AbstractStroke 继承的 protected 方法
                this.strokes,           // 共享同一个列表
                this.indexMap,          // 共享同一个映射
                e,
                vel,
                len
        );

        // 更新共享列表与索引映射
        strokes.add(newStroke);
        indexMap.put(e, newIndex);

        return newStroke;
    }



    @Override
    public int size() {
        return index + 1; // 空笔触 index=-1 → size=0
    }

    @Override
    public PointerEvent getEvent(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
        return strokes.get(index).lastEvent;
    }

    @Override
    public PointerEvent getFirstEvent() {
        return size() == 0 ? null : strokes.get(0).lastEvent;
    }

    @Override
    public float getVelocity(PointerEvent event) {
        Integer idx = indexMap.get(event);
        return idx != null ? strokes.get(idx).velocity : 0f;
    }

    @Override
    public float getCumulativeLength(PointerEvent event) {
        Integer idx = indexMap.get(event);
        return idx != null ? strokes.get(idx).cumulativeLength : 0f;
    }
}