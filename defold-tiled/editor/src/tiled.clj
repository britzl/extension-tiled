;; Copyright 2020 The Defold Foundation
;; Licensed under the Defold License version 1.0 (the "License"); you may not use
;; this file except in compliance with the License.
;;
;; You may obtain a copy of the License, together with FAQs at
;; https://www.defold.com/license
;;
;; Unless required by applicable law or agreed to in writing, software distributed
;; under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
;; CONDITIONS OF ANY KIND, either express or implied. See the License for the
;; specific language governing permissions and limitations under the License.

(prn "TILED EXTENSION PLUGIN!")

(ns editor.tiled
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [editor.protobuf :as protobuf]
            [dynamo.graph :as g]
            [editor.material :as material]
            [editor.math :as math]
            [editor.defold-project :as project]
            [editor.resource :as resource]
            [editor.resource-node :as resource-node]
            [editor.tile-map :as tile-map]
            [editor.tile-map-grid :as tile-map-grid]
            [editor.workspace :as workspace]
            [editor.resource :as resource]
            [editor.system :as system]
            [service.log :as log]
            [internal.util :as util])
  (:import [com.dynamo.gamesys.proto Tile$TileGrid Tile$TileGrid$BlendMode Tile$TileLayer]))

(set! *warn-on-reflection* true)

(def tiled-icon "/defold-tiled/editor/resources/icons/32/Icons_Tiled.png")

(defn invoke-class-method [class-name method-name method-parameter-types method-args]
  (let [class (workspace/load-class! class-name)
        method (. class (getMethod method-name method-parameter-types))]
    (. method (invoke nil method-args))))

(defn load-tiled [project self resource]
  (prn "load-tiled")
  (prn "resource" resource (slurp resource))

  (let [tile-grid (assoc {}
                    :tile-set "foo"
                    :material "/builtins/materials/tile_map.material"
                    :layers [])]
     (prn "GREETING" (invoke-class-method "com.dynamo.bob.pipeline.Tiled" "getGreeting" nil nil))
     (tile-map/load-tile-map project self resource tile-grid)))

(defn write-tiled [lines]
  (prn "write-tiled" lines)
  (string/join "\n" lines))

(defn read-tiled [resource]
  (prn "read-tiled" (slurp resource))
  (slurp resource))


; (defn register-ddf-resource-type [workspace & {:keys [ext node-type ddf-type load-fn dependencies-fn sanitize-fn icon view-types tags tag-opts label] :as args}]
;   (let [read-fn (comp (or sanitize-fn identity) (partial protobuf/read-text ddf-type))
;         args (assoc args
;                :textual? true
;                :load-fn (fn [project self resource]
;                           (let [source-value (read-fn resource)]
;                             (load-fn project self resource source-value)))
;                :dependencies-fn (or dependencies-fn (make-ddf-dependencies-fn ddf-type))
;                :read-fn read-fn
;                :write-fn (partial protobuf/map->str ddf-type))]
;     (apply workspace/register-resource-type workspace (mapcat identity args))))
;
; (defn register-settings-resource-type [workspace & {:keys [ext node-type load-fn icon view-types tags tag-opts label] :as args}]
;   (let [read-fn (fn [resource]
;                   (with-open [setting-reader (io/reader resource)]
;                     (settings-core/parse-settings setting-reader)))
;         args (assoc args
;                :textual? true
;                :load-fn (fn [project self resource]
;                           (let [source-value (read-fn resource)]
;                             (load-fn project self resource source-value)))
;                :read-fn read-fn
;                :write-fn (comp settings-core/settings->str settings-core/settings-with-value))]
;     (apply workspace/register-resource-type workspace (mapcat identity args))))

; (defn register-custom-resource-type [workspace & {:keys [ext node-type load-fn write-fn icon view-types tags tag-opts label] :as args}]
;   (let [read-fn (fn [resource]
;                   (prn "read")
;                   "YAHOO")
;         args (assoc args
;                :textual? true
;                :load-fn (fn [project self resource]
;                           (let [source-value (read-fn resource)]
;                             (load-fn project self resource source-value)))
;                :read-fn read-fn
;                :write-fn write-fn)]
;     (apply workspace/register-resource-type workspace (mapcat identity args))))

(defn register-resource [workspace & {:keys [ext] :as args}]
  (prn "TILED register resource types")
  (let [args (assoc args
               :label "Tiled"
               :icon tiled-icon
               :view-types [:text]
               ; :view-opts {:code {:grammar xml-grammar}}
               :node-type tile-map/TileMapNode
               :load-fn load-tiled
               :read-fn read-tiled
               :write-fn write-tiled
               :textual? true)]
    (apply workspace/register-resource-type workspace (mapcat identity args))))


(g/defnode TiledNode
  (inherits resource-node/ResourceNode)

  (property spine-json resource/Resource
            (value (gu/passthrough spine-json-resource))
            (set (fn [evaluation-context self old-value new-value]
                   (project/resource-setter evaluation-context self old-value new-value
                                            [:resource :spine-json-resource]
                                            [:content :spine-scene]
                                            [:consumer-passthrough :scene-structure]
                                            [:node-outline :source-outline])))
            (dynamic edit-type (g/constantly {:type resource/Resource :ext "json"}))
            (dynamic error (g/fnk [_node-id spine-json]
                             (validate-scene-spine-json _node-id spine-json))))

  (property atlas resource/Resource
            (value (gu/passthrough atlas-resource))
            (set (fn [evaluation-context self old-value new-value]
                   (project/resource-setter evaluation-context self old-value new-value
                                            [:resource :atlas-resource]
                                            [:anim-data :anim-data]
                                            [:gpu-texture :gpu-texture]
                                            [:build-targets :dep-build-targets])))
            (dynamic edit-type (g/constantly {:type resource/Resource :ext "atlas"}))
            (dynamic error (g/fnk [_node-id atlas]
                             (validate-scene-atlas _node-id atlas))))

  ; hidden, used for mapping the default spine.material resource for the .spinescene rendering
  (property material resource/Resource
            (value (gu/passthrough material-resource))
            (set (fn [evaluation-context self old-value new-value]
                   (project/resource-setter evaluation-context self old-value new-value
                                            [:shader :material-shader]
                                            [:samplers :material-samplers])))
            (dynamic edit-type (g/constantly {:type resource/Resource :ext "material"}))
            (dynamic visible (g/constantly false)))

  (property sample-rate g/Num)

  (input tmx-resource resource/Resource)
  (input tile-set-resource resource/Resource)

  (output save-value g/Any produce-save-value)
  (output own-build-errors g/Any produce-scene-own-build-errors)
  (output build-targets g/Any :cached produce-scene-build-targets)
  (output spine-scene-pb g/Any :cached produce-spine-scene-pb)
  (output main-scene g/Any :cached produce-main-scene)
  (output material-shader ShaderLifecycle (gu/passthrough material-shader))
  (output scene g/Any :cached produce-scene)
  (output aabb AABB :cached (g/fnk [spine-scene-pb] (reduce mesh->aabb geom/null-aabb (get-in spine-scene-pb [:mesh-set :mesh-attachments]))))
  (output skin-aabbs g/Any :cached produce-skin-aabbs)
  (output anim-data g/Any (gu/passthrough anim-data))
  (output scene-structure g/Any (gu/passthrough scene-structure))
  (output spine-anim-ids g/Any (g/fnk [scene-structure] (:animations scene-structure))))

(defn register-resource-types [workspace]
  (prn "TILED register resource types")
  (concat
    (register-resource workspace
        :ext "tmx")
    (resource-node/register-ddf-resource-type workspace
      :ext "tiled"
      :build-ext "tiledc"
      :label "Tiled Tile Map"
      :node-type TiledNode
      :ddf-type (workspace/load-class! "com.dynamo.spine.proto.Spine$SpineSceneDesc")
      :load-fn load-spine-scene
      :icon spine-scene-icon
      :view-types [:scene :text]
      :view-opts {:scene {:grid true}}
      :template "/defold-spine/assets/template/template.spinescene")))

; The plugin
(defn load-plugin-tiled [workspace]
  (g/transact (concat (register-resource-types workspace)))
  )

(defn return-plugin []
  (fn [x] (load-plugin-tiled x)))
(return-plugin)
