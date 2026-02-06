import React from "react";
import styles from "../../common/FormElement.module.scss";
import {sendStartDelivery} from "../../../../services/api/backend/blockchain/sendStartDelivery.ts";

interface ConfirmDeliveryFormProps {
    setIsContainerOpen: (value: "createDelivery" | "addPartialDelivery" | "confirmDelivery" | "confirmPartialDelivery" | "") => void;
    deliveryIds: string[];
}

const StartDeliveryForm: React.FC<ConfirmDeliveryFormProps> = ({ setIsContainerOpen, deliveryIds = [] }) => {
    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        const formData = new FormData(e.target as HTMLFormElement);
        const deliveryId = formData.get("deliveryId") as string;

        console.log("Confirming Delivery with ID:", deliveryId);
        sendStartDelivery(deliveryId);
        setIsContainerOpen("");
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>Confirm Delivery</h3>

            <div className={styles.formGroup}>
                <label>Delivery ID</label>
                <select name="deliveryId" required>
                    <option value="">Select Delivery ID</option>
                    {deliveryIds.map((id) => (
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

export default StartDeliveryForm;
