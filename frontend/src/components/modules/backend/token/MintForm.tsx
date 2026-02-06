import React, { useState, useRef } from "react";
import styles from "../../common/FormElement.module.scss";
import { TransactionResponse } from "../../../../model/transactions";
import {mintTransaction} from "../../../../services/api/backend/blockchain/sendBurnTransactions.ts";

interface MintFormData {
    hash: string;
    data: {
        refNumber: string;
        amount: string;
        transferDate: string;
        fromIBAN: string;
        toIBAN: string;
        rawData: { key: string; value: string }[];
    };
}

interface MintFormProps {
    setIsContainerOpen: (open: string) => void;
    transactions: TransactionResponse[];
    availableTransactions: TransactionResponse[];
}

const MintForm: React.FC<MintFormProps> = ({
                                               setIsContainerOpen,
                                               transactions,
                                               availableTransactions,
                                           }) => {
    const [formData, setFormData] = useState<MintFormData>({
        hash: "",
        data: {
            refNumber: "",
            amount: "",
            transferDate: "",
            fromIBAN: "",
            toIBAN: "",
            rawData: [],
        },
    });

    const dateInputRef = useRef<HTMLInputElement | null>(null);

    const handleHashChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedHash = e.target.value;
        setFormData((prev) => ({ ...prev, hash: selectedHash }));
        const selectedTx = availableTransactions.find(
            (tx) => tx.encryptedHash.trim() === selectedHash.trim()
        );
        if (selectedTx) {
            const dt = new Date(selectedTx.data.transferDate);
            const isoString = dt.toISOString();
            const formattedDate = new Date(new Date(isoString).getTime() - new Date().getTimezoneOffset() * 60000)
                .toISOString()
                .slice(0, 16);


            const rawDataArray = selectedTx.data.rawData
                ? Object.entries(selectedTx.data.rawData).map(([key, value]) => ({
                    key,
                    value: String(value),
                }))
                : [];

            setFormData((prev) => ({
                ...prev,
                data: {
                    refNumber: selectedTx.data.refNumber,
                    amount: String(selectedTx.data.amount),
                    transferDate: formattedDate,
                    fromIBAN: selectedTx.data.fromIBAN,
                    toIBAN: selectedTx.data.toIBAN,
                    rawData: rawDataArray,
                },
            }));
        }
    };


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const payload = {
            data: {
                amount: Number(formData.data.amount),
                fromIBAN: formData.data.fromIBAN,
                rawData: Object.fromEntries(
                    formData.data.rawData.map(({ key, value }) => [key, value])
                ),
                refNumber: formData.data.refNumber,
                toIBAN: formData.data.toIBAN,
                transferDate: new Date(formData.data.transferDate).getTime(),

            },
            hash: formData.hash,
        };

        const jsonContent = JSON.stringify(payload);
        console.log("Submitted JSON Content:", jsonContent);
        await mintTransaction(jsonContent);
        setIsContainerOpen("");

    };

    return (
        <form className={`${styles.form} ${styles.Mint}`} onSubmit={handleSubmit}>
            <h3>Mint Tokens</h3>

            {/* Hash Selection */}
            <div className={styles.formGroup}>
                <label>Select Transaction (Hash)</label>
                <select
                    name="hash"
                    value={formData.hash}
                    onChange={handleHashChange}
                    required
                >
                    <option value="">Select a transaction</option>
                    {availableTransactions.map((tx) => (
                        <option key={tx.encryptedHash} value={tx.encryptedHash}>
                            {tx.hash} - {tx.data.refNumber || "no ref"}
                        </option>
                    ))}
                </select>
            </div>

            {/* Confirmation Fields */}
            <div className={styles.formGroup}>
                <label>Reference Number</label>
                <input
                    type="text"
                    name="refNumber"
                    value={formData.data.refNumber}
                    readOnly
                />
            </div>
            <div className={styles.formGroup}>
                <label>Amount</label>
                <input
                    type="number"
                    name="amount"
                    value={formData.data.amount}
                    readOnly
                />
            </div>
            <div className={styles.formGroup}>
                <label>Transfer Date</label>
                <input
                    type="datetime-local"
                    name="transferDate"
                    value={formData.data.transferDate}
                    readOnly
                    ref={dateInputRef}
                />
            </div>
            <div className={styles.formGroup}>
                <label>From IBAN</label>
                <input
                    type="text"
                    name="fromIBAN"
                    value={formData.data.fromIBAN}
                    readOnly
                />
            </div>
            <div className={styles.formGroup}>
                <label>To IBAN</label>
                <input
                    type="text"
                    name="toIBAN"
                    value={formData.data.toIBAN}
                    readOnly
                />
            </div>

            {/* Raw Data Display (read-only) */}
            <div className={styles.rawDataContainer}>
                <label>Raw Data:</label>
                {formData.data.rawData.length > 0 ? (
                    formData.data.rawData.map((item, index) => (
                        <div key={index} className={styles.rawDataRow}>
                            <input type="text" placeholder="Key" value={item.key} readOnly/>
                            <input type="text" placeholder="Value" value={item.value} readOnly/>
                        </div>
                    ))
                ) : (
                    <p>No raw data available.</p>
                )}
            </div>

            {/* Cancel and Submit Buttons */}
            <div className={styles.formButtons}>

                <button
                    type="button"
                    className={styles.buttonRemove}
                    onClick={() => setIsContainerOpen("")}
                >
                    Cancel
                </button>
                <button type="submit" className={styles.submitButton}>
                    Submit
                </button>
            </div>
        </form>
);
};

export default MintForm;
