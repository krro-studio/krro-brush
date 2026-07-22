package top.kzre.krro.brush;

import java.util.ArrayList;
import java.util.IdentityHashMap;

public final class DefaultStroke implements Stroke {
    private final ArrayList<PointerEvent> events;
    private float[] velocities;
    private float[] cumulativeLengths;
    private final IdentityHashMap<PointerEvent, Integer> indexMap;
    private float totalLength = 0f;
    private PointerEvent lastEvent = null;

    private static final int INITIAL_CAPACITY = 128;

    private DefaultStroke() {
        events = new ArrayList<>(INITIAL_CAPACITY);
        velocities = new float[INITIAL_CAPACITY];
        cumulativeLengths = new float[INITIAL_CAPACITY];
        indexMap = new IdentityHashMap<>(INITIAL_CAPACITY);
    }

    public static DefaultStroke create() { return new DefaultStroke(); }

    @Override
    public Stroke push(PointerEvent e) {
        float velocity = 0f;
        if (lastEvent != null) {
            float dx = e.getX() - lastEvent.getX();
            float dy = e.getY() - lastEvent.getY();
            float dist = (float) Math.hypot(dx, dy);
            long dt = e.getTimestamp() - lastEvent.getTimestamp();
            if (dt > 0) velocity = dist / dt;
            totalLength += dist;
        }

        int idx = events.size();
        ensureCapacity(idx);
        events.add(e);
        velocities[idx] = velocity;
        cumulativeLengths[idx] = totalLength;
        indexMap.put(e, idx);       // 使用 IdentityHashMap 记录引用→索引
        lastEvent = e;
        return this;
    }

    @Override
    public int size() { return events.size(); }
    @Override
    public PointerEvent getEvent(int index) { return events.get(index); }
    @Override
    public PointerEvent getFirstEvent() { return events.isEmpty() ? null : events.get(0); }

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
}