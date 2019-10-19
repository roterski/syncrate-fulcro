(ns app.util-test
  (:require
    [clojure.test :refer [deftest]]
    [fulcro-spec.reporters.repl]
    [app.util :as util]
    [fulcro-spec.core :refer [specification provided behavior assertions]]))

(deftest util-uuid-test
  (behavior "returns a valid uuid"
    (assertions
      "when called with no arguments"
      (util/uuid) =fn=> uuid?
      "when called with a string"
      (util/uuid "a7770f7b-0893-428e-9c5b-488ff9803c55") =fn=> uuid?
      "when called with nil"
      (util/uuid nil) =fn=> uuid?
      "when called with un uuid"
      (util/uuid #uuid"a7770f7b-0893-428e-9c5b-488ff9803c55") =fn=> uuid?)))

(comment
  (fulcro-spec.reporters.repl/run-tests))
