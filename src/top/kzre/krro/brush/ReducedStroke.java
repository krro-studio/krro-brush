package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;

import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * 带降采样（最小距离阈值）的不可变笔触。
 * 仅保留与上一个保留点距离超过阈值的点，以减少点数、降低噪声。
 * 采用持久化数据结构，所有版本共享底层事件列表，支持高效追加和查询。
 */
public final class ReducedStroke extends AbstractStroke implements Stroke {

    private final float distanceThreshold;
    // 共享的事件节点列表（所有版本共用）
    private final ArrayList<ReducedStroke> strokes;
    // 共享的事件→索引映射
    private final IdentityHashMap<PointerEvent, Integer> indexMap;
    // 当前节点在 strokes 中的索引（-1 表示空）
    private final int index;
    // 当前事件（空节点为 null）
    private final PointerEvent lastEvent;
    // 当前事件相对前一事件的速度
    private final float velocity;
    // 到当前事件为止的累积路径长度
    private final float cumulativeLength;

    private ReducedStroke(float distanceThreshold,
                          ArrayList<ReducedStroke> strokes,
                          IdentityHashMap<PointerEvent, Integer> indexMap,
                          int index,
                          PointerEvent lastEvent,
                          float velocity,
                          float cumulativeLength,
                          IPersistentMap brushSpec) {
        super(brushSpec);
        this.distanceThreshold = distanceThreshold;
        this.strokes = strokes;
        this.indexMap = indexMap;
        this.index = index;
        this.lastEvent = lastEvent;
        this.velocity = velocity;
        this.cumulativeLength = cumulativeLength;
    }

    /**
     * 创建一个空笔触。
     */
    public static ReducedStroke newInstance(float distanceThreshold, IPersistentMap brushSpec) {
        return new ReducedStroke(
                distanceThreshold,
                new ArrayList<>(),
                new IdentityHashMap<>(),
                -1,
                null,
                0f,
                0f,
                brushSpec
        );
    }

    /**
     * 从现有笔触降采样创建新实例。
     */
    public static ReducedStroke fromStroke(Stroke source, float distanceThreshold, IPersistentMap brushSpec) {
        ReducedStroke reduced = newInstance(distanceThreshold, brushSpec);
        for (int i = 0; i < source.size(); i++) {
            reduced = (ReducedStroke) reduced.append(source.getEvent(i));
        }
        return reduced;
    }

    @Override
    public Stroke append(PointerEvent e) {
        if (lastEvent == null) {
            // 第一个事件，直接添加
            int newIndex = 0;
            ReducedStroke newNode = new ReducedStroke(
                    distanceThreshold,
                    strokes,
                    indexMap,
                    newIndex,
                    e,
                    0f,
                    0f,
                    getBrushSpec()
            );
            strokes.add(newNode);
            indexMap.put(e, newIndex);
            return newNode;
        }

        // 计算与上一个保留点的距离
        float dx = e.getX() - lastEvent.getX();
        float dy = e.getY() - lastEvent.getY();
        float dist = (float) Math.hypot(dx, dy);

        if (dist >= distanceThreshold) {
            // 需要添加新点
            int newIndex = index + 1;
            ReducedStroke newNode = buildReducedStroke(e, dist, newIndex);
            strokes.add(newNode);
            indexMap.put(e, newIndex);
            return newNode;
        } else {
            // 距离不足，不添加新点，返回当前实例
            return this;
        }
    }

    private ReducedStroke buildReducedStroke(PointerEvent e, float dist, int newIndex) {
        float velocity = 0f;
        long dt = e.getTimestamp() - lastEvent.getTimestamp();
        if (dt > 0) {
            velocity = dist / dt;
        }
        float newCumulativeLength = cumulativeLength + dist;
        return new ReducedStroke(
                distanceThreshold,
                strokes,
                indexMap,
                newIndex,
                e,
                velocity,
                newCumulativeLength,
                getBrushSpec()
        );
    }

    @Override
    public int size() {
        return index + 1;
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

    @Override
    public String toString() {
        return "ReducedStroke{threshold=" + distanceThreshold + ", size=" + size() + "}";
    }
}