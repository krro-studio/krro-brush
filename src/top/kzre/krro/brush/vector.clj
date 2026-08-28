(ns top.kzre.krro.brush.vector
  "矢量笔刷：基于动力学处理输入，拟合贝塞尔曲线并生成等弧长宽度采样。
   曲线拟合与宽度采样委托给 top.kzre.krro.brush.VectorStroke。"
  (:require [top.kzre.krro.brush.dynamics :as dynamics])
  (:import (top.kzre.krro.brush VectorStroke)))

(defn- process-events
  "使用动力学规格处理事件序列，返回每个点的宽度列表。"
  [events brush-spec]
  (map #(let [params (dynamics/map-dynamics % brush-spec)
              width  (get params :radius 5.0)]
          (double width))
       events))

(defn generate-vector-stroke
  [brush-spec events & {:keys [max-error width-samples]
                        :or {max-error 4.0 width-samples 100}}]
  (let [widths    (vec (process-events events brush-spec))   ;; 强制求值为 vector
        xs        (double-array (mapv :x events))           ;; mapv 立即求值
        ys        (double-array (mapv :y events))
        result    (VectorStroke/generate xs ys (double-array widths) max-error width-samples)]
    {:curve         (.getCurve result)
     :width-samples (vec (.getWidthSamples result))
     :arc-params    (vec (.getArcParams result))}))

(defn render-vector-stroke
  "根据预处理后的动力学参数序列拟合贝塞尔曲线并采样宽度。
   params 应为已通过动力学映射的 map 序列，每个 map 至少包含 :x, :y, :radius 键。
   若 params 为 nil 或长度 < 2（无法构成有效曲线），则返回 nil。
   可选参数：
     :max-error   - 贝塞尔拟合最大欧氏距离误差，默认 4.0
     :width-samples - 输出的宽度采样点数（实际生成 samples+1 个点），默认 100
   返回（成功时）：
     {:curve         top.kzre.curve.bezier2d.Curve
      :width-samples [Double]
      :arc-params    [Double]}"
  [params & {:keys [max-error width-samples]
             :or {max-error 4.0 width-samples 100}}]
  (when (and (some? params) (>= (count params) 2))
    (let [widths    (double-array (mapv #(get % :radius 5.0) params))
          xs        (double-array (mapv :x params))
          ys        (double-array (mapv :y params))
          result    (VectorStroke/generate xs ys widths max-error width-samples)]
      {:curve         (.getCurve result)
       :width-samples (vec (.getWidthSamples result))
       :arc-params    (vec (.getArcParams result))})))
