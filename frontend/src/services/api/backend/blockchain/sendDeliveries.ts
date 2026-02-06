import { toast } from 'react-toastify';
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";

/**
 * Handles the form submission for creating a delivery.
 *
 * @param formData - The form data collected from the user input.
 * @param setIsContainerOpen - Function to control UI visibility.
 */
export const submitCreateDelivery = async (
    formData: unknown,
    setIsContainerOpen: (open: string) => void
) => {
    try {
        // Convert formData to the required backend format
        const formattedData = {
            channelName: "yfw-channel",
            chaincodeName: "basic",
            deliveryId: formData.deliveryId,
            buyerId: formData.buyerId,
            sellerId: formData.sellerId,
            arbitratorId: formData.arbitratorId,
            tokenAmount: Number(formData.tokenAmount),
            goodsType: formData.goodsType,
            goodsDetails: formData.goodsDetails,
            goodsQuantity: Number(formData.goodsQuantity),
            expiryTimestamp: new Date(formData.expiryTimestamp).getTime(),
            rawData: Object.fromEntries(formData.rawData.map(({ key, value }) => [key, value]))
        };

        const response = await callChaincodeEndpoint(
            'post',
            '/blockchain/createDelivery',
            formattedData.channelName,
            formattedData.chaincodeName,
            formattedData
        );

        toast.success("Delivery created successfully!");
        console.log("Response:", response.data);

        setIsContainerOpen("");
    } catch (error) {
        console.error("Error creating delivery:", error);
        toast.error("Failed to create delivery. " + (error.response?.data || error.message));
    }
};
