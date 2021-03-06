;; Copyright © 2015, JUXT LTD.

(ns yada.dev.website
  (:require [bidi.bidi :refer (RouteProvider tag)]
            [clojure.java.io :as io]
            [com.stuartsierra.component :refer (using)]
            [hiccup.core :refer (html)]
            [modular.bidi :refer (path-for)]
            [modular.component.co-dependency :refer (co-using)]
            [modular.template :as template :refer (render-template)]
            [schema.core :as s]
            yada.bidi
            yada.resources.file-resource
            [yada.yada :as yada]))

(def titles
  {7230 "Hypertext Transfer Protocol (HTTP/1.1): Message Syntax and Routing"
   7231 "Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content"
   7232 "Hypertext Transfer Protocol (HTTP/1.1): Conditional Requests"
   7233 "Hypertext Transfer Protocol (HTTP/1.1): Range Requests"
   7234 "Hypertext Transfer Protocol (HTTP/1.1): Caching"
   7235 "Hypertext Transfer Protocol (HTTP/1.1): Authentication"
   7236 "Initial Hypertext Transfer Protocol (HTTP)\nAuthentication Scheme Registrations"
   7237 "Initial Hypertext Transfer Protocol (HTTP) Method Registrations"
   7238 "The Hypertext Transfer Protocol Status Code 308 (Permanent Redirect)"
   7239 "Forwarded HTTP Extension"
   7240 "Prefer Header for HTTP"})

(defn index [{:keys [*router templater]}]
  (yada/resource
   (fn [ctx]
     ;; TODO: Replace with template resource
     (render-template
      templater
      "templates/page.html.mustache"
      {:content
       (let [header [:button.btn.btn-primary {:onClick "testAll()"} "Repeat tests"]]
         (html
          [:div.container
           [:h2 "Welcome to " [:span.yada "yada"] "!"]
           [:ol
            [:li [:a {:href (path-for @*router :yada.dev.user-manual/user-manual)} "User manual"] " (and " [:a {:href (path-for @*router :yada.dev.user-manual/tests)} "tests"] ")"]
            [:li "HTTP and related specifications"
             [:ul
              [:li [:a {:href "/static/spec/rfc2616.html"} "RFC 2616: Hypertext Transfer Protocol -- HTTP/1.1"]]
              (for [i (range 7230 (inc 7240))]
                [:li [:a {:href (format "/static/spec/rfc%d.html" i)}
                      (format "RFC %d: %s" i (or (get titles i) ""))]])]]
            [:li [:a {:href
                      (format "%s/index.html?url=%s/swagger.json"
                              (path-for @*router :swagger-ui)
                              (path-for @*router :yada.dev.user-api/user-api)
                              )}
                  "Swagger UI"]
             " - to demonstrate Swagger integration"]
            [:li [:a {:href
                      (format "%s/index.html?url=%s/swagger.json"
                              (path-for @*router :swagger-ui)
                              (path-for @*router :hello-api)
                              )}
                  "Swagger UI (hello)"]
             ]]]))}))
   {:id ::index
    :representations [{:content-type #{"text/html"}
                       :charset #{"utf-8"}
                       }]}))

(defrecord Website [*router templater]
  RouteProvider
  (routes [component]
    ["/"
     [["index.html" (index component)]
      ["dir/" (-> (yada.bidi/resource-branch (io/file "dev/resources"))
                  (tag ::dir))]]]))

(defn new-website [& {:as opts}]
  (-> (->> opts
           (merge {})
           (s/validate {})
           map->Website)
      (using [:templater])
      (co-using [:router])))
