(ns extract-domain-url)

(defn extract-domain
  [url]
  (let [regex #"(?:https?://)?([^/:]+)"]
    (some-> (re-find regex url) second)))

(defn- tst []
  (assert (=
(extract-domain "https://www.example.com/path/to/page")
"www.example.com"))

(assert (=
(extract-domain "http://api.github.com:443/users?page=1")
"api.github.com"))

(assert (=
(extract-domain "example.com/about")
"example.com"))

(assert (=
(extract-domain "https://localhost:8080")
"localhost"))

  "SUCCESS")

(tst)
