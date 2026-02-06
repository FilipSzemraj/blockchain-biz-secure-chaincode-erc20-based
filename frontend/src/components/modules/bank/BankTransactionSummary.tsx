import React, { useEffect, useState} from "react";
import styles from "./BankTransactionSummary.module.scss";
import TransactionItem from "./TransactionItem.tsx";
import ReusableWindow from "../common/ReusableWindow.tsx";
import {useBank} from "../../../context/BankContext.tsx";


const BankTransactionSummary: React.FC = () => {
    const [show, setShow] = useState(false);
    const {fetchTransactions, transactions, loading, error, fetched } = useBank();

    useEffect(() => {
        if (show && !fetched) {
            fetchTransactions();

        }
    }, [show, fetched, fetchTransactions]);

    return(

        <div className={styles.wrapper}>
            <ReusableWindow
                isOpen={show}
                onToggle={setShow}
                buttonText="Bank transactions"
                refreshFunction={fetchTransactions}
                classNameWrapper={styles.transactionWrapper}
                classNameContent={styles.transactionContent}
            >
                {loading && <p>Loading transactions...</p>}
                {error && <p className={styles.error}>{error}</p>}
                {!loading && !error && transactions.length === 0 && (
                    <p>No transactions available.</p>
                )}
                {!loading && !error && transactions.length > 0 && (
                    <ul className={styles.transactionList}>
                        {transactions.map((transaction) => (
                            <TransactionItem key={transaction.hash} transaction={transaction} />
                        ))}
                    </ul>
                )}
            </ReusableWindow>
        </div>
    );
}

export default BankTransactionSummary