import React, {useEffect, useMemo, useRef, useState} from "react";
import styles from "../../common/FormElement.module.scss";
import {submitCreateDelivery} from "../../../../services/api/backend/blockchain/sendDeliveries.ts";
import {RootState} from "../../../../redux/store.ts";
import {useSelector} from "react-redux";

interface CreateDeliveryFormProps{
    setIsContainerOpen: (open: string) => void;
    userOrg: string;
}
const CreateDeliveryForm: React.FC<CreateDeliveryFormProps> = ({ setIsContainerOpen, userOrg }) => {

    const {tokenDetails} = useSelector((state: RootState) => state.user);


    const otherOrgs = useMemo(() => {
        const orgs =
            tokenDetails && tokenDetails["orgMspIds"]
            ? (tokenDetails["orgMspIds"] as string[])
            : [];
        return orgs.filter(org => org !== userOrg);
    }, [tokenDetails, userOrg]);


    const dateInputRef = useRef<HTMLInputElement | null>(null);

    const [formData, setFormData] = useState({
        deliveryId: "",
        buyerId: "",
        sellerId: "",
        arbitratorId: "",
        tokenAmount: "",
        goodsType: "",
        goodsDetails: "",
        goodsQuantity: "",
        expiryTimestamp: "",
        rawData: [{ key: "", value: "" }]
    });

    const handleInputSelectChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLInputElement>) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleRawDataChange = (index: number, field: "key" | "value", value: string) => {
        const updatedRawData = [...formData.rawData];
        updatedRawData[index][field] = value;
        setFormData(prev => ({ ...prev, rawData: updatedRawData }));
    };

    const addRawDataField = () => {
        setFormData(prev => ({
            ...prev,
            rawData: [...prev.rawData, { key: "", value: "" }]
        }));
    };

    const removeRawDataField = (index: number) => {
        setFormData(prev => ({
            ...prev,
            rawData: prev.rawData.filter((_, i) => i !== index)
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await submitCreateDelivery(formData, setIsContainerOpen);
    };

    useEffect(() => {
        if (dateInputRef.current) {
            if ("setAttribute" in dateInputRef.current) {
                dateInputRef.current.setAttribute(
                    "min",
                    new Date().toISOString().slice(0, new Date().toISOString().lastIndexOf(":"))
                );
            }
        }
    }, []);

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h3>New Delivery</h3>


            {/* Row 1 */}
            <div className={styles.formGroup}>
                <label>Delivery ID</label>
                <input
                    type="text"
                    name="deliveryId"
                    value={formData.deliveryId}
                    onChange={handleInputChange}
                    required
                />
            </div>
            <div className={styles.formGroup}>
                <label>Buyer ID</label>
                <select name="buyerId" value={formData.buyerId} onChange={handleInputSelectChange} required>
                    <option value="">Select Buyer</option>
                    <option key={userOrg} value={userOrg}>{userOrg}</option>
                </select>
            </div>
            <div className={styles.formGroup}>
                <label>Seller ID</label>
                <select
                    name="sellerId" value={formData.sellerId} onChange={handleInputSelectChange} required>
                    <option value="">Select seller</option>
                    {otherOrgs.map(org => (
                        <option key={org} value={org} disabled={formData.arbitratorId === org}>
                            {org}
                        </option>
                    ))}
                </select>
            </div>

            {/* Row 2 */}
            <div className={styles.formGroup}>
                <label>Arbitrator ID</label>
                <select name="arbitratorId" value={formData.arbitratorId} onChange={handleInputSelectChange} required>
                    <option value="">Select Arbitrator</option>
                    {otherOrgs.map(org => (
                        <option key={org} value={org} disabled={formData.sellerId === org}>
                            {org}
                        </option>
                    ))}
                </select>
            </div>
            <div className={styles.formGroup}>
                <label>Token Amount</label>
                <input
                    type="number"
                    name="tokenAmount"
                    value={formData.tokenAmount}
                    onChange={handleInputChange}
                    required
                />
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

            {/* Row 3 */}
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

            {/* Raw Data Fields */}
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
                            onChange={(e) => handleRawDataChange(index, "value", e.target.value)}
                        />
                        <button type="button" className={styles.buttonRemove}
                                onClick={() => removeRawDataField(index)}>❌
                        </button>
                    </div>
                ))}
                <button type="button" className={styles.addRawDataBtn} onClick={addRawDataField}>+ Add Key-Value
                </button>
            </div>
            <div className={styles.formButtons}>


                <button type="button" className={styles.buttonRemove} onClick={() => setIsContainerOpen("")}>
                    Cancel
                </button>

                <button type="submit" className={styles.submitButton}>Submit</button>
            </div>
        </form>
);
};

export default CreateDeliveryForm;
