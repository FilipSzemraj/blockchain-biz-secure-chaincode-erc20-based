import React, { useState, useRef } from "react";
import styles from "../../common/FormElement.module.scss";
import { Delivery } from "../../../../model/DeliveriesDetails";
import {sendPartialDelivery} from "../../../../services/api/backend/blockchain/sendPartialDelivery.ts";

interface AddPartialDeliveryFormProps {
    setIsContainerOpen: (open: string) => void;
    deliveries: Delivery[];
}

const AddPartialDeliveryForm: React.FC<AddPartialDeliveryFormProps> = ({
                                                                           setIsContainerOpen,
                                                                           deliveries,
                                                                       }) => {
    const [formData, setFormData] = useState({
        deliveryId: "",
        goodsType: "",
        goodsDetails: "",
        goodsQuantity: 0,
        expiryTimestamp: "",
        rawData: [] as { key: string; value: string }[],
    });

    const dateInputRef = useRef<HTMLInputElement | null>(null);

    const handleInputChange = (
        e: React.ChangeEvent<
            HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
        >
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        console.log("AddPartialDelivery form submitted:", formData);
        sendPartialDelivery(formData);
        setIsContainerOpen("");
    };

    const handleRawDataChange = (
        index: number,
        field: string,
        newValue: string
    ) => {
        setFormData((prev) => {
            const updatedRawData = [...prev.rawData];
            updatedRawData[index] = {
                ...updatedRawData[index],
                [field]: newValue,
            };
            return { ...prev, rawData: updatedRawData };
        });
    };

    const addRawDataField = () => {
        setFormData((prev) => ({
            ...prev,
            rawData: [...prev.rawData, { key: "", value: "" }],
        }));
    };

    const removeRawDataField = (index: number) => {
        setFormData((prev) => {
            const updatedRawData = [...prev.rawData];
            updatedRawData.splice(index, 1);
            return { ...prev, rawData: updatedRawData };
        });
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>Add Partial Delivery</h3>

            <div className={styles.formGroup}>
                <label>Delivery ID</label>
                <select
                    name="deliveryId"
                    value={formData.deliveryId}
                    onChange={handleInputChange}
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
                <label>Goods Type</label>
                <input
                    type="text"
                    name="goodsType"
                    value={formData.goodsType}
                    onChange={handleInputChange}
                    required
                />
            </div>

            <div className={styles.formGroup}>
                <label>Goods Details</label>
                <textarea
                    name="goodsDetails"
                    value={formData.goodsDetails}
                    onChange={handleInputChange}
                    required
                />
            </div>

            <div className={styles.formGroup}>
                <label>Goods Quantity</label>
                <input
                    type="number"
                    name="goodsQuantity"
                    value={formData.goodsQuantity}
                    onChange={handleInputChange}
                    required
                />
            </div>

            <div className={styles.formGroup}>
                <label>Expiry Timestamp</label>
                <input
                    type="datetime-local"
                    name="expiryTimestamp"
                    value={formData.expiryTimestamp}
                    onChange={handleInputChange}
                    ref={dateInputRef}
                    required
                />
            </div>

            <div className={styles.rawDataContainer}>
                <label>Raw Data:</label>
                {formData.rawData.map((item, index) => (
                    <div key={index} className={styles.rawDataRow}>
                        <input
                            type="text"
                            placeholder="Key"
                            value={item.key}
                            onChange={(e) => handleRawDataChange(index, "key", e.target.value)}
                        />
                        <input
                            type="text"
                            placeholder="Value"
                            value={item.value}
                            onChange={(e) =>
                                handleRawDataChange(index, "value", e.target.value)
                            }
                        />
                        <button
                            type="button"
                            className={styles.buttonRemove}
                            onClick={() => removeRawDataField(index)}
                        >
                            ❌
                        </button>
                    </div>
                ))}
                <button
                    type="button"
                    className={styles.addRawDataBtn}
                    onClick={addRawDataField}
                >
                    + Add Key-Value
                </button>
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

export default AddPartialDeliveryForm;
