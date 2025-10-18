import {toast} from "react-toastify";
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";


/*{
  "channelName": "yfw-channel",
  "chaincodeName": "basic",
  "deliveryId": "DEL155"
}*/

export const sendConfirmDelivery = async (deliveryId: string): Promise<void> => {
    try{
        await callChaincodeEndpoint(
            "post",
            "/blockchain/confirmDelivery",
            undefined,
            undefined,
            {
                channelName: "yfw-channel",
                chaincodeName: "basic",
                deliveryId: deliveryId,
            }
        )
    }catch(error){
        console.error("Error while starting delivery: ", error);
        toast.error("Error while starting delivery");
        throw error;
    }
}