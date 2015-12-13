FROM clojure


WORKDIR /datomic
ADD https://my.datomic.com/downloads/free/0.9.5130 /datomic/free.jar
RUN unzip /datomic/free.jar && cd datomic-free-0.9.5130 
CMD nohup bin/transactor config/samples/free-transactor-template.properties

WORKDIR /clj
ADD . /clj
VOLUME /clj

# 3449 is default http and websocket port that figwheel uses to communicate
EXPOSE 3449
# 7888 is the default nrepl port
EXPOSE 7888

RUN lein deps
RUN lein cljsbuild once

CMD lein repl && (use 'om-datomic.util) && (init-db) && exit
CMD lein figwheel

# This command will start figwheel and open a repl socket
# This would need to change for a live environment -- you wouldn't want
# the figwheel socket open in production, but I'll leave that bit as an
# exercise for the reader.