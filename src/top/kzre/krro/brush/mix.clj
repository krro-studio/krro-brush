(ns top.kzre.krro.brush.mix
  "混合模式模块：根据 mix-mode 动态计算前景色，并维护状态。
   颜色均为 float[]。完全基于 TiledCanvas。"
  (:require [top.kzre.krro.brush.util :as util])
  (:import (top.kzre.colorutils.color RGB)
           (top.kzre.krro.brush Mixer)))

(defmulti mix
          (fn [brush-spec _params _state _canvas] (:mix-mode brush-spec)))

(defmethod mix :default
  [{:keys [color]} _params state _canvas]
  [color state])

;; 颜色携带
(defmethod mix :colored-brush [brush-spec params state canvas]
  (let [color      (:color brush-spec)
        ratio      (float (util/param :blend-ratio params brush-spec 0.5))
        decay      (float (util/param :carry-decay params brush-spec 0.5))
        exponent   (float (util/param :decay-exponent params brush-spec 1.0))
        min-carry  (float (util/param :min-carry params brush-spec 0.0))
        carry      (or (:carry-color state) (RGB/rgbaToRgb color))
        cx         (float (:x params))
        cy         (float (:y params))
        radius     (float (util/param :sample-radius params brush-spec 0.0))
        fg         (Mixer/coloredBrush canvas color carry cx cy radius ratio decay exponent min-carry)]
    [fg (assoc state :carry-color (RGB/rgbaToRgb fg))]))   ; 更新携带颜色

(defmethod mix :dulling [brush-spec params state canvas]
  (let [color      (:color brush-spec)
        ratio      (float (util/param :dulling-ratio params brush-spec 0.5))
        opacity    (float (util/param :dulling-opacity params brush-spec 1.0))
        cx         (float (:x params))
        cy         (float (:y params))
        radius     (float 0.0)   ;; 默认精确采样
        fg         (Mixer/dullingBrush canvas color cx cy radius ratio opacity)]
    [fg state]))

(defmethod mix :smudge [brush-spec params state canvas]
  (let [color      (:color brush-spec)
        last-params (:last-params state)
        radius     (float (util/param :smudge-radius params brush-spec 10.0))
        strength   (float (util/param :smudge-strength params brush-spec 0.5))
        sample-x   (float (util/param :x last-params params))  ;; 上一个点的坐标
        sample-y   (float (util/param :y last-params params))
        fg         (Mixer/smudgeBrush canvas color sample-x sample-y radius strength)]
    [fg (assoc state :last-params params)]))