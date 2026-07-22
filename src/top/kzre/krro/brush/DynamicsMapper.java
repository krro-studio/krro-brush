package top.kzre.krro.brush;

import clojure.lang.IPersistentMap;

@FunctionalInterface
public interface DynamicsMapper {
    IPersistentMap map(PointerEvent e, IPersistentMap brushSpec);
}
