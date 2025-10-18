import {toast} from "react-toastify";
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";

interface sendResolvePartialDeliveryInterface {
    deliveryId: string,
    partialDeliveryId: string,
    acceptedQuantity: string,
}

/*{
  "channelName": "yfw-channel",
  "chaincodeName": "basic",
  "deliveryId": "DEL123",
  "partialDeliveryId": "PART001",
  "arbitratorAcceptedQuantity": 20
}*/

export const sendResolvePartialDelivery = async (formData: sendResolvePartialDeliveryInterface): Promise<void> => {
    try{
        await callChaincodeEndpoint(
            "post",
            "/blockchain/confirmPartialDelivery",
            undefined,
            undefined,
            {
                channelName: "yfw-channel",
                chaincodeName: "basic",
                deliveryId: formData.deliveryId,
                partialDeliveryId: formData.partialDeliveryId,
                arbitratorAcceptedQuantity: formData.acceptedQuantity
            }
        )
    }catch(error){
        console.error("Error while starting delivery: ", error);
        toast.error("Error while starting delivery");
        throw error;
    }
}