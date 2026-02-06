import React, { useState } from "react";
import styles from "../../common/FormElement.module.scss";
import {
    burnTransaction,
    finalizeBurnTransaction
} from "../../../../services/api/backend/blockchain/sendBurnTransactions.ts";

interface BurnFormProps {
    setIsContainerOpen: (open: string) => void;
}

const BurnForm: React.FC<BurnFormProps> = ({ setIsContainerOpen }) => {
    const [amount, setAmount] = useState<string>("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setAmount(e.target.value);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        console.log("Submitted Burn Payload:", amount);

        await burnTransaction(amount);
        setIsContainerOpen("");
    };

    return (
        <form className={`${styles.form} ${styles.Burn}`} onSubmit={handleSubmit}>
            <h3>Burn Tokens</h3>
            <div className={styles.formGroup}>
                <label>Amount to Burn</label>
                <input
                    type="number"
                    name="amount"
                    value={amount}
                    onChange={handleChange}
                    required
                />
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

export default BurnForm;
