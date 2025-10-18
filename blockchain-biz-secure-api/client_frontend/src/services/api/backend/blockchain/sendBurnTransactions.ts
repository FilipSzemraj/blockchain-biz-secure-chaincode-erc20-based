import apiMain from "../../../../config/axiosMainConfig.ts";
import {toast} from "react-toastify";
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";

export const finalizeBurnTransaction = async (jsonContent: string): Promise<void> => {
    try {
        console.log(jsonContent);

        await callChaincodeEndpoint<string>(
            "post",
            "/blockchain/finalizeBurn",
            "yfw-channel",
            "basic",
            {jsonContent}
        )
    } catch (error) {
        console.error("Error finalizing burn:", error);
        toast.error('Finalization failed: ' + error);
        throw error;
    }
};

export const burnTransaction = async (amountStr: string): Promise<void> => {
    try {
        await callChaincodeEndpoint<string>(
            "post",
            "/blockchain/burn",
            "yfw-channel",
            "basic",
            {amountStr}
        )
    } catch (error) {
        console.error("Error finalizing burn:", error);
        toast.error('Finalization failed: ' + error);
        throw error;
    }
};

export const mintTransaction = async (jsonContent: string): Promise<void> => {
    try {
        console.log(jsonContent);

        await callChaincodeEndpoint<string>(
            "post",
            "/blockchain/mint",
            "yfw-channel",
            "basic",
            {jsonContent}
        )
    } catch (error) {
        console.error("Error minting:", error);
        toast.error('Minting failed: ' + error);
        throw error;
    }
};