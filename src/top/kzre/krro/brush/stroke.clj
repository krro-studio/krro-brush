(ns top.kzre.krro.brush.stroke
  "笔触构建与渲染：完全基于 TiledCanvas。集成 mix 模块，支持动态混色。"
  (:require
    [taoensso.tufte :as p]
    [top.kzre.krro.brush.dab :as dab]
    [top.kzre.krro.brush.mix :as mix]
    [top.kzre.krro.brush.post :as post]
    [top.kzre.krro.brush.util :as util])
  (:import
    (top.kzre.colorutils.blend Blends)
    (top.kzre.krro.brush AbstractStroke PixelPostprocessor Stroke StrokeUtil)
    (top.kzre.krro.util.tile Canvas TiledCanvas)))


(defn join-stroke
  [& strokes]
  (when (seq strokes)
    (let [first-stroke (first strokes)
          brush        (:brush first-stroke)]
      {:brush  brush
       :params (vec (apply concat (map :params strokes)))})))

;; ── 渲染辅助 ─────────────────────────────────────
(defn- preprocess-params [params]
  (-> params
      (update :x #(int (Math/round (double %))))
      (update :y #(int (Math/round (double %))))))

(defn make-post-processor
  [dab-mask brush-spec params ^Canvas canvas]
  (reify PixelPostprocessor
    (process [_ pixel]
      (post/apply-post-pipeline pixel canvas 0 0 dab-mask brush-spec params))))

;; ── 单个 dab 渲染（直接操作 TiledCanvas） ─────────
(defn- render-dab!
  [^Canvas canvas brush stroke-v fg-color]
  (p/profile {:id :krro.brush/render-dab}
             (let [params'        (preprocess-params stroke-v)
                   dab-mask       (p/p :generate-dab (dab/generate-dab brush params'))
                   blend-mode     (util/blend-mode-str brush Blends/NORMAL)
                   extra-opacity  (float (or (util/param :opacity params' brush) 1.0))
                   x              (int (:x params'))
                   y              (int (:y params'))
                   mw             (int (:width dab-mask))
                   mh             (int (:height dab-mask))
                   ^floats mask-data (:data dab-mask)
                   post-processor (make-post-processor dab-mask brush params' canvas)
                   ;; 直接调用 Stroke 的新方法，由 Java 侧处理拷贝与合并
                   dirty-set      (StrokeUtil/stampDab canvas
                                                       mask-data mw mh x y
                                                       fg-color blend-mode extra-opacity
                                                       post-processor)]
               [canvas (set dirty-set)])))

(defn render-stroke
  [^TiledCanvas canvas ^Stroke stroke]
  (let [out-canvas (.copy canvas)
        brush (.getBrushSpec stroke)
        stroke-v (.getStroke stroke)]
    (p/profile {:id :krro.brush/render-stroke}
               (p/p :render-total-stroke
                    (reduce (fn [[^TiledCanvas c dirty] point-params]
                              (p/p :render-single-event
                                   (let [[fg-color _] (p/p :mix-color (mix/mix brush point-params {} c))
                                         [new-c dab-dirty] (p/p :render-dab (render-dab! c brush point-params fg-color))]
                                     [new-c (into dirty dab-dirty)])))
                            [out-canvas #{}]
                            stroke-v)))))