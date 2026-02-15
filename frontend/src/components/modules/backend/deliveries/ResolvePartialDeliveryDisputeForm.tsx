import React, { useState, useMemo } from "react";
import styles from "../../common/FormElement.module.scss";
import { Delivery } from "../../../../model/DeliveriesDetails";
import {sendResolvePartialDelivery} from "../../../../services/api/backend/blockchain/sendResolvePartialDelivery.ts";

interface ResolvePartialDeliveryDisputeFormProps {
    setIsContainerOpen: (
        value:
            | "createDelivery"
            | "addPartialDelivery"
            | "confirmDelivery"
            | "confirmPartialDelivery"
            | "resolvePartialDeliveryDispute"
            | ""
    ) => void;
    deliveries: Delivery[];
}

const ResolvePartialDeliveryDisputeForm: React.FC<ResolvePartialDeliveryDisputeFormProps> = ({
                                                                                                 setIsContainerOpen,
                                                                                                 deliveries,
                                                                                             }) => {
    const [formData, setFormData] = useState({
        deliveryId: "",
        partialDeliveryId: "",
        arbitratorAcceptedQuantity: "",
    });

    // When the delivery selection changes, update the form and reset the partialDeliveryId.
    const handleDeliveryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const newDeliveryId = e.target.value;
        setFormData({
            ...formData,
            deliveryId: newDeliveryId,
            partialDeliveryId: "",
        });
    };

    // Handle changes for number inputs and other input fields.
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // Compute available partial deliveries for the selected delivery.
    const availablePartialDeliveries = useMemo(() => {
        const selectedDelivery = deliveries.find(
            (delivery) => delivery.deliveryId === formData.deliveryId
        );
        // Ensure partialDeliveries is an array, or return an empty array if none exist.
        return selectedDelivery && selectedDelivery.partialDeliveries
            ? selectedDelivery.partialDeliveries
            : [];
    }, [formData.deliveryId, deliveries]);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const arbitratorAcceptedQuantity = Number(formData.arbitratorAcceptedQuantity);
        // Here you would call your chaincode function ResolvePartialDeliveryDispute:
        // e.g., ResolvePartialDeliveryDispute(ctx, deliveryId, partialDeliveryId, arbitratorAcceptedQuantity)
        console.log(
            "Resolving Partial Delivery Dispute with data:",
            formData.deliveryId,
            formData.partialDeliveryId,
            arbitratorAcceptedQuantity
        );
        sendResolvePartialDelivery(formData);
        // After submission, close the form.
        setIsContainerOpen("");
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>Resolve Partial Delivery Dispute</h3>

            {/* Select for Delivery ID */}
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

            {/* Select for Partial Delivery ID */}
            <div className={styles.formGroup}>
                <label>Partial Delivery ID</label>
                <select
                    name="partialDeliveryId"
                    value={formData.partialDeliveryId}
                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                        setFormData({ ...formData, partialDeliveryId: e.target.value })
                    }
                    required
                    disabled={!formData.deliveryId} // Disable until a delivery is selected
                >
                    <option value="">Select Partial Delivery ID</option>
                    {availablePartialDeliveries.map((pd: unknown) => (
                        <option key={pd.deliveryId} value={pd.deliveryId}>
                            {pd.deliveryId}
                        </option>
                    ))}
                </select>
            </div>

            {/* Input for Arbitrator Accepted Quantity */}
            <div className={styles.formGroup}>
                <label>Arbitrator Accepted Quantity</label>
                <input
                    type="number"
                    name="arbitratorAcceptedQuantity"
                    value={formData.arbitratorAcceptedQuantity}
                    onChange={handleInputChange}
                    required
                />
            </div>

            {/* Form Buttons */}
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

export default ResolvePartialDeliveryDisputeForm;
