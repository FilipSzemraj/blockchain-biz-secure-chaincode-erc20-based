FROM hyperledger/fabric-orderer:latest

RUN mkdir -p /etc/hyperledger/scripts
RUN mkdir -p /etc/hyperledger/config

COPY orderer.yaml /etc/hyperledger/config/orderer.yaml

COPY start-orderer.sh /etc/hyperledger/scripts/start-orderer.sh

COPY config.yaml /etc/hyperledger/config/



WORKDIR /etc/hyperledger/scripts


#RUN mkdir -p server client

#COPY fabric-ca-server-config.yaml /etc/hyperledger/server/

#EXPOSE 7055

#CMD ["sh", "-c", "fabric-ca-server start -b admin:adminpw -d"]
