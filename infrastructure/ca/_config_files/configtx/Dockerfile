FROM hyperledger/fabric-tools:2.5

RUN mkdir -p /etc/hyperledger/scripts
RUN mkdir -p /etc/hyperledger/config
RUN mkdir -p /etc/hyperledger/clients-certs/

COPY configtx.yaml /etc/hyperledger/config/configtx.yaml
RUN ls -l /etc/hyperledger/config/configtx.yaml || (echo "File configtx.yaml not found!" && exit 1)


COPY creating-channel.sh /etc/hyperledger/scripts/creating-channel.sh
COPY healthcheck-admin.sh /etc/hyperledger/scripts/healthcheck-admin.sh
RUN ls -l /etc/hyperledger/scripts/creating-channel.sh || (echo "File creating-channel.sh not found!" && exit 1)


WORKDIR /etc/hyperledger/scripts