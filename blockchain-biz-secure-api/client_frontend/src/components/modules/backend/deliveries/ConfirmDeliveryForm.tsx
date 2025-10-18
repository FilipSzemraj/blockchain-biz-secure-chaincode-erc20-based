import React, { useState } from "react";
import styles from "../../common/FormElement.module.scss";
import {sendConfirmDelivery} from "../../../../services/api/backend/blockchain/sendConfirmDelivery.ts";

interface ConfirmDeliveryFormProps {
    setIsContainerOpen: (value: "createDelivery" | "addPartialDelivery" | "confirmDelivery" | "confirmPartialDelivery" | "") => void;
    deliveryIds: string[];
}

const ConfirmDeliveryForm: React.FC<ConfirmDeliveryFormProps> = ({ setIsContainerOpen, deliveryIds = [] }) => {
    const [formData, setFormData] = useState({
        deliveryId: "",
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const formData = new FormData(e.target as HTMLFormElement);
        const deliveryId = formData.get("deliveryId") as string;
        console.log("Confirming Delivery with ID:", deliveryId);

        sendConfirmDelivery(deliveryId);
        setIsContainerOpen("");
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>Confirm Delivery</h3>

            <div className={styles.formGroup}>
                <label>Delivery ID</label>
                <select
                    name="deliveryId"
                    value={formData.deliveryId}
                    onChange={handleInputChange}
                    required
                >
                    <option value="">Select Delivery ID</option>
                    {deliveryIds && deliveryIds.map((id) => (
                        <option key={id} value={id}>
                            {id}
                        </option>
                    ))}
                </select>
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

export default ConfirmDeliveryForm;
