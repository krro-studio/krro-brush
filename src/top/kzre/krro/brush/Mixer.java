package top.kzre.krro.brush;

import top.kzre.colorutils.color.RGB;
import top.kzre.krro.util.tile.TiledCanvas;

public final class Mixer {
    private Mixer() {}

    /**
     * 执行完整的 colored‑brush 混色，包括背景采样、颜色混合与衰减。
     * @param canvas       目标画布
     * @param brushColor   笔刷颜色 RGBA
     * @param carryColor   当前携带颜色 RGB（不含 Alpha）
     * @param cx, cy       采样中心坐标（世界像素）
     * @param radius       采样半径（0 表示仅单个像素）
     * @param blendRatio   前景与背景的混合比率
     * @param carryDecay   携带颜色衰减系数
     * @param decayExponent 衰减指数
     * @param minCarry     最小衰减值
     * @return             最终混合后的 RGBA 颜色
     */
    public static float[] coloredBrush(
            TiledCanvas canvas,
            float[] brushColor,
            float[] carryColor,
            float cx, float cy, float radius,
            float blendRatio,
            float carryDecay,
            float decayExponent,
            float minCarry) {

        float[] bg = sampleGaussian(canvas, cx, cy, radius);
        float[] fgRgb = RGB.rgbaToRgb(brushColor);
        float[] bgRgb = RGB.rgbaToRgb(bg);
        float[] blended = RGB.mix(fgRgb, bgRgb, blendRatio);

        float effectiveDecay = (float) Math.pow(carryDecay, decayExponent);
        float clampedDecay = Math.max(minCarry, effectiveDecay);
        float[] finalRgb = RGB.mix(carryColor, blended, clampedDecay);

        return RGB.withAlpha(finalRgb, RGB.alpha(brushColor));
    }

    /**
     * Dulling 混色：将背景色与笔刷颜色按比例混合，然后应用额外不透明度。
     * @param canvas        目标画布
     * @param brushColor    笔刷颜色 RGBA
     * @param cx, cy        采样中心坐标
     * @param radius        采样半径（0 表示仅单个像素）
     * @param dullingRatio  背景色占比（0~1）
     * @param dullingOpacity 最终透明度乘子
     * @return              混合后的 RGBA 颜色
     */
    public static float[] dullingBrush(
            TiledCanvas canvas,
            float[] brushColor,
            float cx, float cy, float radius,
            float dullingRatio,
            float dullingOpacity) {

        float[] bg = sampleGaussian(canvas, cx, cy, radius);
        float[] fgRgb = RGB.rgbaToRgb(brushColor);
        float[] bgRgb = RGB.rgbaToRgb(bg);
        float[] dulled = RGB.mix(bgRgb, fgRgb, dullingRatio); // bg 在比例中占主要
        float alpha = RGB.alpha(brushColor) * dullingOpacity;
        return RGB.withAlpha(dulled, alpha);
    }

    /**
     * Smudge 混色：采样指定位置的颜色，然后与笔刷颜色按强度混合。
     * @param canvas         目标画布
     * @param brushColor     笔刷颜色 RGBA
     * @param sampleX, sampleY 采样中心坐标（通常为上一事件的位置）
     * @param smudgeRadius   采样半径
     * @param smudgeStrength 混合强度（0~1，值越大采样颜色越浓）
     * @return               混合后的 RGBA 颜色
     */
    public static float[] smudgeBrush(
            TiledCanvas canvas,
            float[] brushColor,
            float sampleX, float sampleY, float smudgeRadius,
            float smudgeStrength) {

        float[] src = sampleGaussian(canvas, sampleX, sampleY, smudgeRadius);
        float[] fgRgb = RGB.rgbaToRgb(brushColor);
        float[] srcRgb = RGB.rgbaToRgb(src);
        float[] mixed = RGB.mix(fgRgb, srcRgb, smudgeStrength);
        return RGB.withAlpha(mixed, RGB.alpha(brushColor));
    }

    /**
     * 高斯加权采样（供内部及外部使用）。
     */
    public static float[] sampleGaussian(TiledCanvas canvas,
                                         float cx, float cy, float radius) {
        if (radius <= 0.0f) {
            // 精确采样单个像素
            float[] pixel = new float[4];
            canvas.getPixel(Math.round(cx), Math.round(cy), pixel);
            return pixel;
        }

        int r = (int) Math.ceil(radius);
        int x0 = (int) (cx - r);
        int y0 = (int) (cy - r);
        int x1 = (int) (cx + r);
        int y1 = (int) (cy + r);
        int w = x1 - x0 + 1;
        int h = y1 - y0 + 1;
        if (w <= 0 || h <= 0) return RGB.rgba(0, 0, 0, 0);

        float[] region = new float[w * h * 4];
        canvas.readBytes(region, 0, x0, y0, w, h, w);

        float sigma = radius / 2.0f;
        float sumR = 0, sumG = 0, sumB = 0, sumA = 0, sumW = 0;

        for (int y = 0; y < h; y++) {
            int worldY = y0 + y;
            for (int x = 0; x < w; x++) {
                int worldX = x0 + x;
                float dx = worldX - cx;
                float dy = worldY - cy;
                float dist2 = dx * dx + dy * dy;
                if (dist2 > radius * radius) continue;

                float weight = (float) Math.exp(-0.5f * dist2 / (sigma * sigma));
                int idx = (y * w + x) * 4;
                sumR += weight * region[idx];
                sumG += weight * region[idx + 1];
                sumB += weight * region[idx + 2];
                sumA += weight * region[idx + 3];
                sumW += weight;
            }
        }

        if (sumW <= 0) return RGB.rgba(0, 0, 0, 0);
        float invW = 1.0f / sumW;
        return RGB.rgba(sumR * invW, sumG * invW, sumB * invW, sumA * invW);
    }

}