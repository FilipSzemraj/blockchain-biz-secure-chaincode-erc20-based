FROM hyperledger/fabric-tools:2.5

RUN mkdir -p /etc/hyperledger/scripts
RUN mkdir -p /etc/hyperledger/config
RUN mkdir -p /etc/hyperledger/clients-certs/

COPY configtx.yaml /etc/hyperledger/config/configtx.yaml
RUN ls -l /etc/hyperledger/config/configtx.yaml || (echo "File configtx.yaml not found!" && exit 1)


COPY creating-channel-with-retry.sh /etc/hyperledger/scripts/creating-channel-with-retry.sh
COPY healthcheck-admin-smart.sh /etc/hyperledger/scripts/healthcheck-admin-smart.sh
RUN ls -l /etc/hyperledger/scripts/creating-channel-with-retry.sh || (echo "File creating-channel-with-retry.sh not found!" && exit 1)
RUN chmod +x /etc/hyperledger/scripts/*.sh


WORKDIR /etc/hyperledger/scripts