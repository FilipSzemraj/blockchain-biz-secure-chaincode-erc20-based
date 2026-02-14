FROM hyperledger/fabric-peer:2.5

# No Java needed here — chaincode is compiled by fabric-javaenv image,
# not by the peer itself. The peer only packages source into .tar.gz
# and delegates compilation to fabric-javaenv during `peer lifecycle chaincode install`.

RUN mkdir -p /etc/hyperledger/scripts
RUN mkdir -p /etc/hyperledger/config

COPY core.yaml /etc/hyperledger/config/core.yaml

COPY start-peer.sh /etc/hyperledger/scripts/start-peer.sh
COPY fetch-block-as-peer.sh /etc/hyperledger/scripts/fetch-block-as-peer.sh

COPY config.yaml /etc/hyperledger/config/

RUN chmod +x /etc/hyperledger/scripts/*.sh

WORKDIR /etc/hyperledger/scripts