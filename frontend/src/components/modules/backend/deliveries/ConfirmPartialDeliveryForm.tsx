import React, { useState, useMemo } from "react";
import styles from "../../common/FormElement.module.scss";
import { Delivery } from "../../../../model/DeliveriesDetails";
import {sendConfirmPartialDelivery} from "../../../../services/api/backend/blockchain/sendConfirmPartialDelivery.ts";

interface ConfirmPartialDeliveryFormProps {
    setIsContainerOpen: (
        value:
            | "createDelivery"
            | "addPartialDelivery"
            | "confirmDelivery"
            | "confirmPartialDelivery"
            | ""
    ) => void;
    deliveries: Delivery[];
}

const ConfirmPartialDeliveryForm: React.FC<ConfirmPartialDeliveryFormProps> = ({
                                                                                   setIsContainerOpen,
                                                                                   deliveries,
                                                                               }) => {
    const [formData, setFormData] = useState({
        deliveryId: "",
        partialDeliveryId: "",
        acceptedQuantity: "",
    });

    const handleDeliveryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const newDeliveryId = e.target.value;
        setFormData({
            ...formData,
            deliveryId: newDeliveryId,
            partialDeliveryId: "",
        });
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const availablePartialDeliveries = useMemo(() => {
        const selectedDelivery = deliveries.find(
            (delivery) => delivery.deliveryId === formData.deliveryId
        );
        return selectedDelivery && selectedDelivery.partialDeliveries
            ? selectedDelivery.partialDeliveries
            : [];
    }, [formData.deliveryId, deliveries]);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const acceptedQuantity = Number(formData.acceptedQuantity);

        console.log(
            "Confirming Partial Delivery with data:",
            formData.deliveryId,
            formData.partialDeliveryId,
            acceptedQuantity
        );
        sendConfirmPartialDelivery(formData);
        setIsContainerOpen("");
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>Confirm Partial Delivery</h3>

            <div className={styles.formGroup}>
                <label>Delivery ID</label>
                <select
                    name="deliveryId"
                    value={formData.deliveryId}
                    onChange={handleDeliveryChange}
                    required
                >
                    <option value="">Select Delivery ID</option>
                    {deliveries.map((delivery) => (
                        <option key={delivery.deliveryId} value={delivery.deliveryId}>
                            {delivery.deliveryId}
                        </option>
                    ))}
                </select>
            </div>

            <div className={styles.formGroup}>
                <label>Partial Delivery ID</label>
                <select
                    name="partialDeliveryId"
                    value={formData.partialDeliveryId}
                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                        setFormData({ ...formData, partialDeliveryId: e.target.value })
                    }
                    required
                    disabled={!formData.deliveryId}
                >
                    <option value="">Select Partial Delivery ID</option>
                    {availablePartialDeliveries.map((pd: any) => (
                        <option key={pd.deliveryId} value={pd.deliveryId}>
                            {pd.deliveryId}
                        </option>
                    ))}
                </select>
            </div>

            <div className={styles.formGroup}>
                <label>Accepted Quantity</label>
                <input
                    type="number"
                    name="acceptedQuantity"
                    value={formData.acceptedQuantity}
                    onChange={handleInputChange}
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

export default ConfirmPartialDeliveryForm;
