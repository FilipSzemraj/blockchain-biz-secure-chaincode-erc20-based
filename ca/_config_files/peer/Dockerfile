FROM hyperledger/fabric-peer:2.5

RUN mkdir -p /etc/hyperledger/scripts
RUN mkdir -p /etc/hyperledger/config

COPY core.yaml /etc/hyperledger/config/core.yaml

COPY start-peer.sh /etc/hyperledger/scripts/start-peer.sh
COPY add-anchor-peers.sh /etc/hyperledger/scripts/add-anchor-peers.sh

COPY config.yaml /etc/hyperledger/config/



WORKDIR /etc/hyperledger/scripts


#RUN mkdir -p server client

#COPY fabric-ca-server-config.yaml /etc/hyperledger/server/

#EXPOSE 7055

#CMD ["sh", "-c", "fabric-ca-server start -b admin:adminpw -d"]
