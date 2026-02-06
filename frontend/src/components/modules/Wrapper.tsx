import React from "react";
import BankTransactionSummary from "./bank/BankTransactionSummary.tsx";
import styles from "./Wrapper.module.scss";
import BackendUI from "./backend/BackendUI.tsx";
import DeliveryBox from "./backend/deliveries/DeliveryBox.tsx";
import TokenBox from "./backend/token/TokenBox.tsx";
import {BankProvider} from "../../context/BankContext.tsx";
import BlockchainTransactions from "./backend/transactions/BlockchainTransactions.tsx";
const Wrapper : React.FC = () => {
    return(
        <div className={styles.container}>
            <div className={styles.container__tokenDetails}>
                <BackendUI />
            </div>
            <BankProvider>
                <BankTransactionSummary/>
                <DeliveryBox />
                <TokenBox />
                <BlockchainTransactions />
            </BankProvider>
        </div>
    );
}

export default Wrapper;