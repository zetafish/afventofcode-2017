(ns advent.day12
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.set :as set]))

(def parser
  (insta/parser
    "<S> = number <spaces arrow spaces> number (<comma spaces> number)*
     <arrow> = '<->'
     number = #'[0-9]+'
     <spaces> = ' '+
     <comma> = ','"))

(def small ["0 <-> 2"
            "1 <-> 1"
            "2 <-> 0, 3, 4"
            "3 <-> 2, 4"
            "4 <-> 2, 3, 6"
            "5 <-> 6"
            "6 <-> 4, 5"])

(def input (-> (io/resource "input12.txt")
               (slurp)
               (str/split-lines)))

(defn parse
  [s]
  (->>
    (parser s)
    (map (comp #(Integer/parseInt %) second))))

(defn add-link
  [graph [x y]]
  (-> graph
      (update x #(set (cons y %)))
      (update y #(set (cons x %)))))

(defn add-rule
  [graph [x & xs]]
  (let [pairs (mapv #(vector x %) xs)]
    (reduce add-link graph pairs)))

(defn graph
  [rules]
  (reduce add-rule {} (map parse rules)))

(defn build-group
  [graph n]
  (letfn [(f [seen todo]
            (if (empty? todo)
              seen
              (let [around (apply set/union (map graph todo))]
                (recur (set/union seen around)
                       (set/difference around todo seen)))))]
    (sort (f #{} [n]))))

(defn all-groups
  [graph]
  (letfn [(f [groups unseen]
            (if (empty? unseen)
              groups
              (let [group (build-group graph (first unseen))]
                (recur (cons group groups)
                       (set/difference unseen group)))))]
    (f [] (set (keys graph)))))

(build-group (graph small) 0)
(build-group (graph input) 0)

(all-groups (graph small))
(count (all-groups (graph input)))
