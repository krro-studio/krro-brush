package top.kzre.krro.brush;

import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * 带降采样（最小距离阈值）的笔触。
 * 仅保留与上一个保留点距离超过阈值的点，以减少点数、降低噪声。
 * 速度和累积长度基于保留的点重新计算，不受被丢弃点影响。
 */
public final class ReducedStroke implements Stroke {

    private final ArrayList<PointerEvent> events;
    private float[] velocities;
    private float[] cumulativeLengths;
    private final IdentityHashMap<PointerEvent, Integer> indexMap;
    private float totalLength = 0f;
    private final float distanceThreshold;

    private static final int INITIAL_CAPACITY = 128;

    public ReducedStroke(float distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
        events = new ArrayList<>(INITIAL_CAPACITY);
        velocities = new float[INITIAL_CAPACITY];
        cumulativeLengths = new float[INITIAL_CAPACITY];
        indexMap = new IdentityHashMap<>(INITIAL_CAPACITY);
    }

    public static ReducedStroke fromStroke(Stroke source, float distanceThreshold) {
        ReducedStroke reduced = new ReducedStroke(distanceThreshold);
        for (int i = 0; i < source.size(); i++) {
            reduced.push(source.getEvent(i));
        }
        return reduced;
    }

    @Override
    public Stroke push(PointerEvent e) {
        if (events.isEmpty()) {
            addPoint(e, 0f, 0f);
            return this;
        }

        PointerEvent last = events.get(events.size() - 1);
        float dx = e.getX() - last.getX();
        float dy = e.getY() - last.getY();
        float dist = (float) Math.hypot(dx, dy);

        if (dist >= distanceThreshold) {
            float velocity = 0f;
            long dt = e.getTimestamp() - last.getTimestamp();
            if (dt > 0) {
                velocity = dist / dt;
            }
            float newLength = totalLength + dist;
            addPoint(e, velocity, newLength);
        }
        return this;
    }

    private void addPoint(PointerEvent event, float velocity, float newLength) {
        int idx = events.size();
        ensureCapacity(idx);
        events.add(event);
        velocities[idx] = velocity;
        cumulativeLengths[idx] = newLength;
        indexMap.put(event, idx);               // ★ 建立引用到索引的映射
        totalLength = newLength;
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public PointerEvent getEvent(int index) {
        return events.get(index);
    }

    @Override
    public PointerEvent getFirstEvent() {
        return events.isEmpty() ? null : events.get(0);
    }

    @Override
    public float getVelocity(PointerEvent event) {
        Integer idx = indexMap.get(event);
        return idx != null ? velocities[idx] : 0f;
    }

    @Override
    public float getCumulativeLength(PointerEvent event) {
        Integer idx = indexMap.get(event);
        return idx != null ? cumulativeLengths[idx] : 0f;
    }

    private void ensureCapacity(int idx) {
        if (idx >= velocities.length) {
            int newCap = velocities.length * 2;
            float[] newVel = new float[newCap];
            float[] newLen = new float[newCap];
            System.arraycopy(velocities, 0, newVel, 0, velocities.length);
            System.arraycopy(cumulativeLengths, 0, newLen, 0, cumulativeLengths.length);
            velocities = newVel;
            cumulativeLengths = newLen;
        }
    }

    @Override
    public String toString() {
        return "ReducedStroke{threshold=" + distanceThreshold + ", size=" + size() + "}";
    }
}