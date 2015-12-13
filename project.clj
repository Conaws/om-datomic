(defproject om-datomic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3195"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.8.8"]
                 [expectations "2.1.4"]
                 [racehub/om-bootstrap "0.5.3"]
                 [ring "1.3.2"]
                 [compojure "1.3.1"]
                 [com.datomic/datomic-free "0.9.5130" :exclusions [joda-time]]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.2.9"]]


  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/out"
                                    "resources/public/js/main.js"]

  :figwheel {:ring-handler om-datomic.core/handler

             :nrepl-host "0.0.0.0"
             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888
             :repl false


  }



  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/clj" "src/cljs"]
                        :figwheel true
                        :compiler {:output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :main om-datomic.core
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}]})
