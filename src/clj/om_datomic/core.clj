(ns om-datomic.core
  (:require [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes GET PUT POST DELETE]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.edn :as edn]
            [datomic.api :as d]))


(def uri "datomic:free://localhost:4334/om_datomic")
(def conn (d/connect uri))

(defn index []
  (file-response "public/html/index.html" {:root "resources"}))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn update-class [id params]
  (let [db    (d/db conn)
        title (:class/title params)
        eid   (ffirst
                (d/q '[:find ?class
                       :in $ ?id
                       :where
                       [?class :class/id ?id]]
                  db id))]
    (d/transact conn [[:db/add eid :class/title title]])
    (generate-response {:status :ok})))




;; (defn create-class [params]
;;   (let [db    (d/db conn)
;;         title (:class/title params)
;;         id    (:class/id params)]
;;     (d/transact conn [[:db/add eid :class/title title]])
;;     (generate-response {:status :ok})))


(defn retract-class [id]
  (let [db    (d/db conn)
        eid   (ffirst
                (d/q '[:find ?class
                       :in $ ?id
                       :where
                       [?class :class/id ?id]]
                  db id))]
    (d/transact conn [[:db/retract eid :class/id id]])
    (generate-response {:status :ok})))



(defn create-class [title id]
    (d/transact conn [{:db/id (d/tempid :db.part/user -1)
                     :class/title title
                     :class/id id}])
  (generate-response {:status :ok}))




(defn get-class-titles []
  (let [classes (d/q '[:find ?title
                      :where
                     [_ :class/title ?title]]
                  (d/db conn))]
    classes))

(defn classes []
  (let [db (d/db conn)
        classes
        (vec (map #(d/touch (d/entity db (first %)))
               (d/q '[:find ?class
                      :where
                      [?class :class/id]]
                 db)))]
    (generate-response classes)))



(defroutes routes
  (GET "/" [] (index))
  (GET "/classes" [] (classes))
  (POST "/classes" {params :edn-body} (create-class (:class/title params)(:class/id params)))
  (PUT "/class/:id/update" {params :params edn-body :edn-body}(update-class (:id params) edn-body))
  (PUT "/class/:id/retract"  {params :params} (retract-class (:id params)))
  (route/files "/" {:root "resources/public"}))

(defn read-inputstream-edn [input]
  (edn/read
   {:eof nil}
   (java.io.PushbackReader.
    (java.io.InputStreamReader. input "UTF-8"))))

(defn parse-edn-body [handler]
  (fn [request]
    (handler (if-let [body (:body request)]
               (assoc request
                 :edn-body (read-inputstream-edn body))
               request))))

(def handler
  (-> routes
      parse-edn-body))
