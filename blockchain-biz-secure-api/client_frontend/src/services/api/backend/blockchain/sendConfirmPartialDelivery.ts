import {toast} from "react-toastify";
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";

interface sendConfirmPartialDeliveryInterface {
    deliveryId: string,
    partialDeliveryId: string,
    acceptedQuantity: string,
}

/*{
  "channelName": "yfw-channel",
  "chaincodeName": "basic",
  "deliveryId": "DEL155",
  "partialDeliveryId": "DEL155_part_1_Tue Feb 18 20:43:45 UTC 2025",
  "acceptedQuantity": 10
}*/

export const sendConfirmPartialDelivery = async (formData: sendConfirmPartialDeliveryInterface): Promise<void> => {
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
                acceptedQuantity: formData.acceptedQuantity
            }
        )
    }catch(error){
        console.error("Error while starting delivery: ", error);
        toast.error("Error while starting delivery");
        throw error;
    }
}