(ns wgctrl.cluster.utils
	(:gen-class)
	(:require 
    [wgctrl.utils.main :as u]
    [wgctrl.utils.ipcalc :as ip]
    [wgctrl.utils.subnet :as subnet]))

(defn interface-size 
  "Count of peers on iface"
  [iface]
  (count @(.peers iface)))

(defn node-size 
"Calculates size of node -> sum if all peers"
  [node]
  (reduce (fn [n i] (+ (interface-size i) n))
    0 @(.interfaces node)))

(defn endpoints4
 "Gets intet emdpoint address"
 [node]
	(map #(-> % :endpoint :inet ) (.interfaces node)))

(defn addr! 
  "Calculates available IP address for client"
  [interface]
  (let [subnet (.subnet interface)
        size (interface-size interface)
        addresses (->> (map (fn [x]
                             (-> x
                                 .allowed-ips 
                                 subnet/addr
                                 ip/addr->int)) @(.peers interface))
                     sort
                     (map #(ip/int->addr %)))]

              (cond 
                (= 0 size) (ip/addr++ (subnet/addr (:inet subnet)))
                (>= size (subnet/size (subnet/mask (:inet subnet)))) nil
                :else (ip/addr++ (reduce (fn [a a'] (if (> (Math/abs (ip/addr- a' a)) 1)
                                             a
                                             a')) (first addresses) (rest addresses))))))

