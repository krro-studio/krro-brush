package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.java.api.Clojure;

@FunctionalInterface
public interface DynamicsMapper {
    IPersistentMap map(PointerEvent e, IPersistentMap brushSpec, Stroke stroke);

    // ── 静态常量：事件属性关键字 ──
    Keyword KW_X = Keyword.intern("x");
    Keyword KW_Y = Keyword.intern("y");
    Keyword KW_PRESSURE = Keyword.intern("pressure");
    Keyword KW_TILT_X = Keyword.intern("tilt-x");
    Keyword KW_TILT_Y = Keyword.intern("tilt-y");
    Keyword KW_ROTATION = Keyword.intern("rotation");
    Keyword KW_VELOCITY = Keyword.intern("velocity");
    Keyword KW_CUMULATIVE_LENGTH = Keyword.intern("cumulative-length");
    Keyword KW_TIMESTAMP = Keyword.intern("timestamp");

    static DynamicsMapper instance() {
        return Holder.INSTANCE;
    }

    final class Holder {
        private static final DynamicsMapper INSTANCE;
        private static final IFn mapDynamicsFn;
        static {
            IFn require = Clojure.var("clojure.core", "require");
            require.invoke(Clojure.read("top.kzre.krro.brush.dynamics"));
            mapDynamicsFn = Clojure.var("top.kzre.krro.brush.dynamics", "map-dynamics");

            INSTANCE = (event, brushSpec, stroke) -> {
                IPersistentMap eventMap = eventToMap(event, stroke);
                return (IPersistentMap) mapDynamicsFn.invoke(eventMap, brushSpec);
            };
        }
    }

    /** 将 PointerEvent 和 Stroke 中的物理量合并为 Clojure map（使用预定义关键字） */
    static IPersistentMap eventToMap(PointerEvent e, Stroke stroke) {
        float velocity = stroke.getVelocity(e);
        float cumulativeLength = stroke.getCumulativeLength(e);
        return PersistentHashMap.create(
                KW_X, e.getX(),
                KW_Y, e.getY(),
                KW_PRESSURE, e.getPressure(),
                KW_TILT_X, e.getTiltX(),
                KW_TILT_Y, e.getTiltY(),
                KW_ROTATION, e.getRotation(),
                KW_VELOCITY, velocity,
                KW_CUMULATIVE_LENGTH, cumulativeLength,
                KW_TIMESTAMP, e.getTimestamp()
        );
    }
}