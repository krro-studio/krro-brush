package top.kzre.krro.brush;

import clojure.lang.IPersistentVector;

public interface Stroke {

    Stroke append(PointerEvent e);
    int size();
    PointerEvent getEvent(int index);
    PointerEvent getFirstEvent();

    /** 根据事件引用获取瞬时速度（像素/毫秒） */
    float getVelocity(PointerEvent event);

    /** 根据事件引用获取累积路径长度（像素） */
    float getCumulativeLength(PointerEvent event);

    default boolean isEmpty() { return size() == 0; }

    IPersistentVector getStroke();
}