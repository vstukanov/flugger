(defproject flugger "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-protobuf "0.5.0"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; Async
                 [manifold "0.1.8"]

                 ;; Loging
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]

                 ;; Database
                 [alaisi/postgres.async "0.8.0"]
                 [honeysql "0.9.4"]

                 ;; HTTP
                 [http-kit "2.3.0"]
                 [stylefruits/gniazdo "1.1.1"]

                 ;; RPC
                 [com.google.protobuf/protobuf-java "3.6.1"]
                 [clojusc/protobuf "3.6.0-v1.2-SNAPSHOT"]]
  :protoc "protoc"
  :main ^:skip-aot flugger.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
