import React, { useState } from "react";
import styles from "../../common/FormElement.module.scss";

interface TransferFormProps {
    setIsContainerOpen: (
        value: "mint" | "burn" | "finalizeBurn" | "transfer" | ""
    ) => void;
    organizations: string[];
    onSubmit: (to: string, value: number) => void;
}

const TransferForm: React.FC<TransferFormProps> = ({ setIsContainerOpen, organizations, onSubmit }) => {
    const [formData, setFormData] = useState({
        to: "",
        value: "",
    });

    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const numericValue = Number(formData.value);
        onSubmit(formData.to, numericValue);
        setIsContainerOpen("");
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>Transfer Tokens</h3>

            <div className={styles.formGroup}>
                <label>Recipient (To)</label>
                <select
                    name="to"
                    value={formData.to}
                    onChange={handleInputChange}
                    required
                >
                    <option value="">Select Organization</option>
                    {organizations.map((org) => (
                        <option key={org} value={org}>
                            {org}
                        </option>
                    ))}
                </select>
            </div>

            <div className={styles.formGroup}>
                <label>Amount</label>
                <input
                    type="number"
                    name="value"
                    value={formData.value}
                    onChange={handleInputChange}
                    placeholder="Enter transfer amount"
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

export default TransferForm;
