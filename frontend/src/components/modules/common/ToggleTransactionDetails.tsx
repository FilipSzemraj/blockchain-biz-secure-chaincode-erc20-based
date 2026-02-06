import React, {useEffect, useState} from "react";
import styles from "./ToggleTransactionDetails.module.scss";

import { TransactionResponse } from "../../../model/transactions";
import { BurnRequestWithHash } from "../../../model/TokenDetails";

type TransactionOrBurn = TransactionResponse | BurnRequestWithHash;

interface ToggleTransactionDetailsProps {
    transaction: TransactionOrBurn
    onToggle?: () => void
    defaultExpanded?: boolean;
}

const ToggleTransactionDetails: React.FC<ToggleTransactionDetailsProps> = ({ transaction, onToggle, defaultExpanded }) => {
    const [expanded, setExpanded] = useState(defaultExpanded);
    const toggleExpanded = () => setExpanded((prev) => !prev);

    useEffect(() => {
        if (onToggle) {
            onToggle();
        }
    }, [expanded]);

    const renderShort = () => (
        <span className={styles.shortHash}>
      {transaction.hash.substring(0, 20)}...
    </span>
    );

    const renderDetails = () => {
        if ("data" in transaction) {
            const { data, hash, encryptedHash } = transaction;
            return (
                <div className={styles.detailsContainer}>
                    <div>
                        <strong>Hash:</strong> {hash}
                    </div>
                    <div>
                        <strong>Encrypted Hash:</strong> {encryptedHash}
                    </div>
                    <div>
                        <strong>Reference:</strong> {data.refNumber}
                    </div>
                    <div>
                        <strong>Amount:</strong> {data.amount}
                    </div>
                    <div>
                        <strong>From IBAN:</strong> {data.fromIBAN}
                    </div>
                    <div>
                        <strong>To IBAN:</strong> {data.toIBAN}
                    </div>
                    <div>
                        <strong>Transfer Date:</strong> {data.transferDate}
                    </div>
                    <div>
                        <strong>Raw Data:</strong>
                        <div className={styles.rawData}>
                            <div>
                                <strong>Additional Info:</strong> {data.rawData.additionalInfo}
                            </div>
                            <div>
                                <strong>Currency:</strong> {data.rawData.currency}
                            </div>
                            <div>
                                <strong>Burn Request Hash:</strong> {data.rawData.BurnRequestHash}
                            </div>
                        </div>
                    </div>
                </div>
            );
        } else if ("burnRequest" in transaction) {
            const { burnRequest, hash } = transaction;
            return (
                <div className={styles.detailsContainer}>
                    <div>
                        <strong>Hash:</strong> {hash}
                    </div>
                    <div>
                        <strong>Burn Request ID:</strong> {burnRequest.id}
                    </div>
                    <div>
                        <strong>Burn Wallet:</strong> {burnRequest.burnWallet}
                    </div>
                    <div>
                        <strong>Recipient Account:</strong> {burnRequest.recipientAccount}
                    </div>
                    <div>
                        <strong>Amount:</strong> {burnRequest.amount}
                    </div>
                    <div>
                        <strong>Timestamp:</strong> {new Date(burnRequest.timestamp).toLocaleString()}
                    </div>
                </div>
            );
        }
        return null;
    };

    return (
        <div onClick={toggleExpanded} className={styles.toggleContainer}>
            {expanded ? renderDetails() : renderShort()}
        </div>
    );
};

export default ToggleTransactionDetails;
