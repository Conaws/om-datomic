(ns ^:figwheel-always om-datomic.core
    (:require [cljs.reader :as reader]
              [goog.events :as events]
              [om.core :as om :include-macros true]
              [om-bootstrap.button :as b]
              [om-bootstrap.panel :as p]
              [om-tools.dom :as d :include-macros true]
              [om.dom :as dom :include-macros true])
    (:import [goog.net XhrIo]
             goog.net.EventType
             [goog.events EventType]))


(enable-console-print!)

(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})



(defn edn-xhr [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
      (fn [e]
        (on-complete (reader/read-string (.getResponseText xhr)))))
    (. xhr
      (send url (meths method) (when data (pr-str data))
        #js {"Content-Type" "application/edn"}))))


(def app-state
  (atom {:classes []}))


(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))


(defn handle-change [e data edit-key owner]
  (om/transact! data edit-key (fn [_] (.. e -target -value))))

(defn end-edit [text owner cb]
  (om/set-state! owner :editing false)
  (cb text))


(defn editable [data owner {:keys [edit-key on-edit on-delete] :as opts}]
  (reify
    om/IInitState
    (init-state [_]
                {:editing false})
    om/IRenderState
    (render-state [_ {:keys [editing]}]
                  (let [text (get data edit-key)]
                    (d/li nil
                            (dom/span #js {:style (display (not editing))} text)
                            (dom/input  #js {:style (display editing)
                                            :value text
                                            :onChange #(handle-change % data edit-key owner)
                                            :onKeyDown #(when (= (.-key %) "Enter")
                                                          (end-edit text owner on-edit))
                                            :onBlur #(when (om/get-state owner :editing)
                                                         (end-edit text owner on-edit))})
                            (b/dropdown  {:title "danger"}
                                        (b/menu-item {:key "1" :style (display (not editing))
                                             :onClick #(om/set-state! owner :editing true)}
                                        "Edit")
                                        (b/menu-item {:bs-style "danger"
                                       :onClick #(on-delete)}
                                      "Destory the Class")))))))

(defn retract-class [classes id]

  (om/transact! classes [] #(vec (filter (fn [val] (not= id (:class/id val))) %)))
;; (swap! app-state assoc :classes (filter #(not= id (:class/id %)) (:classes @app-state)))
 (edn-xhr
    {:method :put
     :url (str "class/" id "/retract")
     :data {}
     :on-complete
     (fn [res]
       (println "server response:" res))}))

(defn on-edit [id title]
    (edn-xhr
    {:method :put
     :url (str "class/" id "/update")
     :data {:class/title title}
     :on-complete
     (fn [res]
       (println "server response:" res))}))




(defn class-form [classes owner]
  (d/form
          (dom/label nil "ID:")
          (dom/input #js {:ref "class-id"})
          (dom/label nil "Name:")
          (dom/input #js {:ref "class-name"})
          (b/button {:onClick (fn [e] (create-class classes owner))}
           "Add A Great Class")))


(defn classes-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
                (edn-xhr {:method :get
                          :url "classes"
                          :on-complete #(om/transact! app :classes (fn [_] %))}))
    om/IRender
    (render [_]
      (dom/div #js {:id "classes"}
        (dom/h2 nil "Classes")
        (apply dom/ul nil
          (map
            (fn [class]
              (let [id (:class/id class)]
                (om/build editable class
                  {:opts {:edit-key :class/title
                          :on-edit #(on-edit id %)
                          :on-delete #(retract-class (:classes app) id)}})))
            (:classes app)))
        (d/div
         (p/panel {:header (d/h3 "Add A Class")}
            (class-form (:classes app) owner)))))))





(defn create-class [classes owner]
  (let [class-id-el   (om/get-node owner "class-id")
        class-id      (.-value class-id-el)
        class-name-el (om/get-node owner "class-name")
        class-title    (.-value class-name-el)
        new-class     {:class/id class-id :class/title class-title}]
    (om/transact! classes [] #(conj % new-class)
      [:create new-class])
    (on-create class-id class-title)
;;     (println "What we're sending" class-id class-title)
    (set! (.-value class-id-el) "")
    (set! (.-value class-name-el) "")))


(defn on-create [id title]
    (edn-xhr
    {:method :post
     :url (str "classes")
     :data {:class/title title :class/id id}
     :on-complete
     (fn [res]
       (println "server response:" res))}))






(om/root classes-view app-state
  {:target (.getElementById js/document "classes")})


(println "Hello world!")
