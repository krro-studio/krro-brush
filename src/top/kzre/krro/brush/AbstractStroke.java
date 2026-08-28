package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentVector;

import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * 抽象笔触，根据笔刷配置转换笔触参数
 */
public abstract class AbstractStroke implements Stroke {
    private final DynamicsMapper mapper = DynamicsMapper.instance();
    private final IdentityHashMap<PointerEvent, IPersistentMap> cache;
    private final IPersistentMap brushSpec;

    protected AbstractStroke(IPersistentMap brushSpec) {
        this.brushSpec = brushSpec;
        this.cache = new IdentityHashMap<>();
    }
    @Override
    public IPersistentVector getStroke() {
        int n = this.size();
        ArrayList<IPersistentMap> list = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            PointerEvent e = this.getEvent(i);
            list.add(getStrokeEvent(e));  // 触发缓存计算
        }
        return PersistentVector.create(list);
    }

    public IPersistentMap getStrokeEvent(PointerEvent e) {
        return cache.computeIfAbsent(e, k -> mapper.map(k, brushSpec, this));
    }

    @Override
    public IPersistentMap getBrushSpec(){
        return this.brushSpec;
    }
}
