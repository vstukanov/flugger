(ns flugger.jsonrpc.errors)

(defn rpc-error [code message context & {:keys [http-code]}]
  (ex-info message {:rpc true
                    :code code
                    :context context
                    :http-code (or http-code 500)}))

(defn parse-error [message context]
  (rpc-error -32700
             (format "Parser error: %s" message)
             context))

(defn invalide-request-error [message context]
  (rpc-error -32600
             (format "Invalid Request: %s" message)
             context
             :http-code 400))

(defn method-not-found-error [context]
  (rpc-error -32601
             "Method not found."
             context
             :http-code 401))

(defn invalid-params-error [message context]
  (rpc-error -32602
             (format "Invalid params: %s" message)
             context))

(defn internal-error [message context]
  (rpc-error -32603
             (format "Internal error: %s" message)
             context))

(defn server-error [message context]
  (rpc-error -32099
             (format "Server error: %s" message)
             context))
