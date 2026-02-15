import {useState} from "react";
import styles from "./BankTransactionSummary.module.scss";

const TransactionItem = ({ transaction }) => {
    const [showFullHash, setShowFullHash] = useState(false);

    return (
        <li key={transaction.hash} className={styles.transactionItem}>
            <p><strong>Reference:</strong> {transaction.data.refNumber}</p>
            <p><strong>Amount:</strong> {transaction.data.amount} {transaction.data.rawData.currency}</p>
            <p><strong>From:</strong> {transaction.data.fromIBAN}</p>
            <p><strong>To:</strong> {transaction.data.toIBAN}</p>
            <p><strong>Date:</strong> {transaction.data.transferDate}</p>
            <p><strong>Additional Info:</strong> {transaction.data.rawData.additionalInfo}</p>
            <p>
                <strong>Encrypted hash:</strong>{" "}
                <span
                    onClick={() => setShowFullHash(!showFullHash)}
                    style={{cursor: "pointer", color: "#1a88ff", textDecoration: "underline"}}
                >
                    {showFullHash ? transaction.encryptedHash : `${transaction.encryptedHash.substring(0, 20)}...`}
                </span>
            </p>
            <p>
                <strong>hash:</strong>{" "}
                <span
                    onClick={() => setShowFullHash(!showFullHash)}
                    style={{cursor: "pointer", color: "#1a88ff", textDecoration: "underline"}}
                >
                    {showFullHash ? transaction.hash : `${transaction.hash.substring(0, 20)}...`}
                </span>
            </p>
        </li>
    );
};

export default TransactionItem;