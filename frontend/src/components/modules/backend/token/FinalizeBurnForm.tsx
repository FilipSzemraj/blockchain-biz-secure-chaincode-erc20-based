import React, { useState, useRef } from "react";
import styles from "../../common/FormElement.module.scss";
import { TransactionResponse } from "../../../../model/transactions.ts";
import { finalizeBurnTransaction } from "../../../../services/api/backend/blockchain/sendBurnTransactions.ts";

interface FinalizeBurnFormProps {
    setIsContainerOpen: (open: string) => void;
    availableBurnRequests: TransactionResponse[]; // Use TransactionResponse
}

interface FinalizeBurnFormData {
    encryptedHash: string;
    data: {
        refNumber: string;
        amount: string;
        transferDate: string;
        fromIBAN: string;
        toIBAN: string;
        rawData: { key: string; value: string }[];
    };
}

const FinalizeBurnForm: React.FC<FinalizeBurnFormProps> = ({
                                                               setIsContainerOpen,
                                                               availableBurnRequests,
                                                           }) => {
    const [formData, setFormData] = useState<FinalizeBurnFormData>({
        encryptedHash: "",
        data: {
            refNumber: "",
            amount: "",
            transferDate: "",
            fromIBAN: "",
            toIBAN: "",
            rawData: [],
        },
    });

    //console.log("availableBurnRequests:"+JSON.stringify(availableBurnRequests));

    const dateInputRef = useRef<HTMLInputElement | null>(null);

    const handleSelectionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedEncryptedHash = e.target.value;
        const selectedTransaction = availableBurnRequests.find(
            (tx) => tx.encryptedHash.trim() === selectedEncryptedHash.trim()
        );

        if (selectedTransaction) {
            const { data, encryptedHash } = selectedTransaction;
            const formattedDate = new Date(data.transferDate - new Date().getTimezoneOffset() * 60000)
                .toISOString().slice(0, 16);
            const rawDataArray = [
                ...Object.entries(data.rawData).map(([key, value]) => ({ key, value }))
            ];
            //const rawDataArray = [{ key: "BurnRequestHash", value: data.rawData.BurnRequestHash }];

            setFormData({
                encryptedHash: encryptedHash, // Use encryptedHash
                data: {
                    refNumber: data.refNumber,
                    amount: data.amount.toString(),
                    transferDate: formattedDate,
                    fromIBAN: data.fromIBAN,
                    toIBAN: data.toIBAN,
                    rawData: rawDataArray,
                },
            });
        } else {
            setFormData((prev) => ({ ...prev, encryptedHash: selectedEncryptedHash }));
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
            hash: formData.encryptedHash,
        };

        const jsonContent = JSON.stringify(payload);
        console.log("Submitted FinalizeBurn JSON Content:", jsonContent);
        await finalizeBurnTransaction(jsonContent);
        setIsContainerOpen("");
    };

    return (
        <form className={`${styles.form} ${styles.FinalizeBurn}`} onSubmit={handleSubmit}>
            <h3>Finalize Burn</h3>

            <div className={styles.formGroup}>
                <label>Select Burn Request</label>
                <select
                    name="burnRequest"
                    value={formData.encryptedHash}
                    onChange={handleSelectionChange}
                    required
                >
                    {availableBurnRequests.length < 1 ? (
                        <option value="">There are no transactions matching</option>
                    ) : (
                        <option value="">Select a burn request</option>
                    )}

                    {availableBurnRequests.map((tx) => (
                        <option key={tx.encryptedHash} value={tx.encryptedHash}>
                            {tx.data.refNumber} - {tx.encryptedHash}
                        </option>
                    ))}
                </select>
            </div>

            <div className={styles.formGroup}>
                <label>Reference Number</label>
                <input type="text" name="refNumber" value={formData.data.refNumber} readOnly />
            </div>
            <div className={styles.formGroup}>
                <label>Amount</label>
                <input type="number" name="amount" value={formData.data.amount} readOnly />
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
                <input type="text" name="fromIBAN" value={formData.data.fromIBAN} readOnly />
            </div>
            <div className={styles.formGroup}>
                <label>To IBAN</label>
                <input type="text" name="toIBAN" value={formData.data.toIBAN} readOnly />
            </div>

            {/* Display rawData (read-only) */}
            <div className={styles.rawDataContainer}>
                <label>Raw Data:</label>
                {formData.data.rawData.length > 0 ? (
                    formData.data.rawData.map((item, index) => (
                        <div key={index} className={styles.rawDataRow}>
                            <input type="text" placeholder="Key" value={item.key} readOnly />
                            <input type="text" placeholder="Value" value={item.value} readOnly />
                        </div>
                    ))
                ) : (
                    <p>No raw data available.</p>
                )}
            </div>

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

export default FinalizeBurnForm;