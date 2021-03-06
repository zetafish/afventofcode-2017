(ns advent.day14
  (:require [advent.day10 :refer [knot-hash]]
            [clojure.string :as str]
            [clojure.set :as set]))

;; Suddenly, a scheduled job activates the system's disk
;; defragmenter. Were the situation different, you might sit and watch
;; it for a while, but today, you just don't have that kind of
;; time. It's soaking up valuable system resources that are needed
;; elsewhere, and so the only option is to help it finish its task as
;; soon as possible.

;; The disk in question consists of a 128x128 grid; each square of the
;; grid is either free or used. On this disk, the state of the grid is
;; tracked by the bits in a sequence of knot hashes.

;; A total of 128 knot hashes are calculated, each corresponding to a
;; single row in the grid each hash contains 128 bits which correspond
;; to individual grid squares. Each bit of a hash indicates whether
;; that square is free (0) or used (1).

;; The hash inputs are a key string (your puzzle input), a dash, and a
;; number from 0 to 127 corresponding to the row. For example, if your
;; key string were flqrgnkx, then the first row would be given by the
;; bits of the knot hash of flqrgnkx-0, the second row from the bits
;; of the knot hash of flqrgnkx-1, and so on until the last row,
;; flqrgnkx-127.

;; The output of a knot hash is traditionally represented by 32
;; hexadecimal digits; each of these digits correspond to 4 bits, for
;; a total of 4 * 32 = 128 bits. To convert to bits, turn each
;; hexadecimal digit to its equivalent binary value, high-bit first: 0
;; becomes 0000, 1 becomes 0001, e becomes 1110, f becomes 1111, and
;; so on a hash that begins with a0c2017... in hexadecimal would begin
;; with 10100000110000100000000101110000... in binary.


(def x "flqrgnkx")

(defn row
  [x n]
  (str x "-" n))

(defn one-bits
  [n]
  (->> (Integer/toString n 2)
       (filter #(= \1 %))
       (count)))

(def ones (mapv one-bits (range 16)))

(defn hex
  [ch]
  (Integer/parseInt (str ch) 16))

(defn bits
  [ch]
  (let [b (Integer/toString (hex ch) 2)]
    (str/join (concat (repeat (- 4 (count b)) \0) b))))

(defn knot-hash-bits
  [s]
  (->> (knot-hash s)
       (map bits)
       (str/join)
       (map #(- (int %) (int \0)))
       (vec)))

(defn knot-hash-ones
  [s]
  (->> (knot-hash s)
       (map hex)
       (map ones)
       (reduce +)))

(defn part-1
  [input]
  (->> (range 128)
       (map #(row input %))
       (map knot-hash-ones)
       (reduce +)))

(defn build-grid
  [input]
  (->> (range 128)
       (map #(row input %))
       (mapv knot-hash-bits)))

(comment
  (part-1 "flqrgnkx") ;; => 8108
  (part-1 "hxtvlmkl")) ;; => 8214

(def grid (build-grid "flqrgnkx"))

(def g0(->> grid
            (take 8)
            (mapv #(vec (take 8 %)))))

(defn around
  [[x y] n]
  (->> [[-1 0] [1 0] [0 1] [0 -1]]
       (map (fn [[dx dy]] [(+ x dx) (+ y dy)]))
       (filter (fn [[x y]]
                 (and (<= 0 x (dec n))
                      (<= 0 y (dec n)))))))

(defn find-region
  [grid size point]
  (loop [region #{}
         seen #{}
         [p & ps] [point]]
    (cond
      (nil? p)
      region

      (zero? (get-in grid p))
      (recur region (conj seen p) ps)

      :else (let [qs (set/difference (set (around p size)) seen)]
              (recur (conj region p)
                     (set/union seen qs)
                     (concat ps qs))))))

(defn all-points
  [size]
  (set (for [x (range size)
             y (range size)]
         [x y])))

(defn all-regions
  [grid size]
  (loop [regions []
         unseen (all-points size)]
    (let [unseen (set (remove #(zero? (get-in grid %)) unseen))]
      (if (empty? unseen)
        regions
        (let [region (find-region grid size (first unseen))]
          (recur (conj regions region)
                 (set/difference unseen (set region))))))))

(defn part-2
  [input]
  (let [grid (build-grid input)]
    (count (all-regions grid 128))))

(comment
  (part-2 "flqrgnkx") ;; => 1242
  (part-2 "hxtvlmkl")) ;; => 1093
