package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentVector;

import java.util.ArrayList;
import java.util.IdentityHashMap;

public final class DynamicsStroke implements Stroke {
    private final Stroke inner;
    private final DynamicsMapper mapper;
    private final IPersistentMap brushSpec;
    private final IdentityHashMap<PointerEvent, IPersistentMap> cache;

    public DynamicsStroke(Stroke inner, DynamicsMapper mapper, IPersistentMap brushSpec) {
        this.inner = inner;
        this.mapper = mapper;
        this.brushSpec = brushSpec;
        this.cache = new IdentityHashMap<>();
    }

    @Override
    public Stroke push(PointerEvent e) {
        inner.push(e);   // 转发，不在此计算
        return this;
    }

    /**
     * 获取指定事件的动力学映射结果（带缓存）。
     * 若已缓存则直接返回，否则调用 mapper 计算并缓存。
     */
    public IPersistentMap getParams(PointerEvent e) {
        return cache.computeIfAbsent(e, k -> mapper.map(k, brushSpec));
    }

    public IPersistentVector getParamsVector() {
        int n = inner.size();
        ArrayList<IPersistentMap> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            PointerEvent e = inner.getEvent(i);
            list.add(getParams(e));  // 触发缓存计算
        }
        return PersistentVector.create(list);
    }

    // ── Stroke 接口委托 ──
    @Override public int size() { return inner.size(); }
    @Override public PointerEvent getEvent(int index) { return inner.getEvent(index); }
    @Override public PointerEvent getFirstEvent() { return inner.getFirstEvent(); }
    @Override public float getVelocity(PointerEvent event) { return inner.getVelocity(event); }
    @Override public float getCumulativeLength(PointerEvent event) { return inner.getCumulativeLength(event); }
    @Override public String toString() { return "DynamicsStroke{" + inner + "}"; }
}