/*
{
    "channelName": "yfw-channel",
    "chaincodeName": "basic",
    "deliveryId": "DEL155",
    "goodsType": "Tools",
    "goodsDetails": "Saw",
    "goodsQuantity": 10,
    "expiryTimestamp": 1743465600000,
    "rawData": {
    "warehouse": "StockRoom-3",
    "notes": "Checked packaging quality"
}
}
*/

import {toast} from "react-toastify";
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";

interface formDataInterface{
    deliveryId: string,
    goodsType: string,
    goodsDetails: string,
    goodsQuantity: number,
    expiryTimestamp: string,
    rawData: {key: string; value: string}[],
}

export const sendPartialDelivery = async (formData: formDataInterface)
: Promise<void> => {

    const data = {
        channelName: "yfw-channel",
        chaincodeName: "basic",
        deliveryId: formData.deliveryId,
        goodsType: formData.goodsType,
        goodsDetails: formData.goodsDetails,
        goodsQuantity: formData.goodsQuantity,
        expiryTimestamp: new Date(formData.expiryTimestamp).getTime(),
        rawData: Object.fromEntries(formData.rawData.map(({key, value}) => [key, value]))
}
    try{
        await callChaincodeEndpoint<string>(
            "post",
            "/blockchain/addPartialDelivery",
            undefined,
            undefined,
            data,
        )

        toast.success("sended partial delivery")
    }catch(error){
        console.error("Error while adding partial delivery: ", error);
        toast.error("Error while adding partial delivery");
        throw error;
    }
}
