package top.kzre.krro.brush;

/**
 * 不可变的值对象，表示一个输入事件（来自数位板、鼠标或其他指针设备）。
 * <p>
 * 包含所有常用物理和软件计算参数，与 Krita 的 KisPaintInformation 类保持概念一致。
 * 通过建造者模式（Builder）创建实例。
 */
public final class PointerEvent {

    public enum EventType {
        DOWN,
        MOVE,
        UP
    }

    // 物理/辅助字段
    private final float x;
    private final float y;
    private final float pressure;
    private final float tiltX;
    private final float tiltY;
    private final float rotation;
    private final long  timestamp;
    private final EventType type;

    // 私有构造器，仅供 Builder 使用
    private PointerEvent(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.pressure = builder.pressure;
        this.tiltX = builder.tiltX;
        this.tiltY = builder.tiltY;
        this.rotation = builder.rotation;
        this.timestamp = builder.timestamp;
        this.type = builder.type;
    }

    /** 获取一个新的 Builder 实例 */
    public static Builder newBuilder() {
        return new Builder();
    }

    // ---------- Getter ----------
    public float getX()              { return x; }
    public float getY()              { return y; }
    public float getPressure()       { return pressure; }
    public float getTiltX()          { return tiltX; }
    public float getTiltY()          { return tiltY; }
    public float getRotation()       { return rotation; }
    public long  getTimestamp()      { return timestamp; }
    public EventType getType()       { return type; }

    @Override
    public String toString() {
        return String.format("PointerEvent{x=%.2f, y=%.2f, pressure=%.2f, type=%s}",
                x, y, pressure, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointerEvent)) return false;
        PointerEvent that = (PointerEvent) o;
        return Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0 &&
                Float.compare(that.pressure, pressure) == 0 &&
                Float.compare(that.tiltX, tiltX) == 0 &&
                Float.compare(that.tiltY, tiltY) == 0 &&
                Float.compare(that.rotation, rotation) == 0 &&
                timestamp == that.timestamp &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(x);
        result = 31 * result + Float.hashCode(y);
        result = 31 * result + Float.hashCode(pressure);
        result = 31 * result + Float.hashCode(tiltX);
        result = 31 * result + Float.hashCode(tiltY);
        result = 31 * result + Float.hashCode(rotation);
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + type.hashCode();
        return result;
    }

    // ---------- Builder ----------
    public static final class Builder {
        private float x;
        private float y;
        private float pressure = 1.0f;
        private float tiltX;
        private float tiltY;
        private float rotation;
        private long  timestamp;
        private EventType type = EventType.MOVE;

        // 设置字段，返回 Builder 自身支持链式调用
        public Builder x(float val)          { this.x = val; return this; }
        public Builder y(float val)          { this.y = val; return this; }
        public Builder pressure(float val)   { this.pressure = val; return this; }
        public Builder tiltX(float val)      { this.tiltX = val; return this; }
        public Builder tiltY(float val)      { this.tiltY = val; return this; }
        public Builder rotation(float val)   { this.rotation = val; return this; }
        public Builder timestamp(long val)   { this.timestamp = val; return this; }
        public Builder type(EventType val)   { this.type = val; return this; }

        /** 构建不可变的 PointerEvent 实例 */
        public PointerEvent build() {
            return new PointerEvent(this);
        }
    }
}