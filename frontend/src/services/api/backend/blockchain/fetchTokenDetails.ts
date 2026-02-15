import {BurnRequestWithHash, BurnResponse, TokenDetailsResponse} from "../../../../model/TokenDetails.ts";
import apiMain from "../../../../config/axiosMainConfig.ts";
import {callChaincodeEndpoint} from "../../../../config/apiChaincode.ts";
import {
    BlockchainTransfer,
    ConfirmationWithHashDTO,
    MintResponseDTO,
    TransactionResponse
} from "../../../../model/transactions.ts";
import {toast} from "react-toastify";

/**
 * Fetches token details from the blockchain via the backend API.
 * @returns Promise<Map<string, string>> - A promise resolving to a map of token details.
 */
export const fetchTokenDetails = async (): Promise<Map<string, string | string[]>> => {
    try {
        const requestBody = {
            channelName: "yfw-channel",
            chaincodeName: "basic",
            functionName: "TokenDetails",
            args: []
        };

        const response = await apiMain.post<TokenDetailsResponse>('/blockchain/query', requestBody);

        const resultMap = new Map<string, string | string[]>();

        if (response.data) {
            Object.entries(response.data).forEach(([key, value]) => {
                if (key === "orgMspIds" && typeof value === "string") {
                    try {
                        resultMap.set(key, JSON.parse(value) as string[]);
                    } catch (error) {
                        toast.error(`Error parsing orgMspIds: ${value}: `+error);

                        console.error(`Error parsing orgMspIds: ${value}`, error);
                        resultMap.set(key, []);
                    }
                } else if (typeof value === "string") {
                    resultMap.set(key, value);
                }
            });
        }

        return resultMap;
    } catch (error) {
        toast.error('Error fetching token details: '+error);
        console.error("Error fetching token details:", error);
        throw error; // Rethrow the error for further handling
    }
};

export const fetchMintList = async (): Promise<TransactionResponse[]> => {
    try {
        const response = await callChaincodeEndpoint<MintResponseDTO>('get', "/blockchain/mintList")
        //console.log('Mint list retrieved:', response.data);
        const mintArray: TransactionResponse[] = response.data.mints.map((mint: ConfirmationWithHashDTO) => ({
            data: mint.confirmation,
            encryptedHash: "",
            hash: mint.hash
        }));
        return mintArray;
    } catch (error) {
        toast.error('Error fetching mint list: '+error);
        console.error('Error fetching mint list:', error);
        throw error;
    }
}

/**
 * Fetches the list of minted transaction indexes from the backend.
 *
 * @param {string} channelName - The channel name to use in the query.
 * @param {string} chaincodeName - The chaincode name to use in the query.
 * @returns {Promise<Array<string>>} A promise that resolves to the list of minted transaction indexes.
 */
export const fetchMintListIndex = async (): Promise<string[]> => {
    try {
        const response = await callChaincodeEndpoint<string[]>('get', "/blockchain/mintIndexList")
        console.log('Mint list retrieved:', response.data);
        return response.data;
    } catch (error) {
        toast.error('Error fetching mint list: '+error);

        console.error('Error fetching mint list:', error);
        throw error;
    }
}

/**
 * Fetches the list of finalized burn transaction indexes from the backend.
 *
 * @returns {Promise<Array<string>>} A promise that resolves to the list of finalized burn transaction indexes.
 */
export const fetchFinalizedBurnList = async (): Promise<BurnRequestWithHash[]> => {
    try {
        const response = await callChaincodeEndpoint<BurnResponse>('get', "/blockchain/finalizedBurnList");
        //console.log('Finalized burn list retrieved:', response.data);
        //const burnArray: BurnRequestWithHash[] = response.data.map(item => JSON.parse(item));
        return response.data.burns;
    } catch (error) {
        toast.error('Error fetching finalized burn list: '+error);

        console.error('Error fetching finalized burn list:', error);
        throw error;
    }
};

/**
 * Fetches the list of pending burn transactions from the backend.
 *
 * @returns {Promise<Array<BurnRequestWithHash>>} A promise that resolves to the list of pending burn transactions.
 */
export const fetchPendingBurnList = async (): Promise<BurnRequestWithHash[]> => {
    try {
        const response = await callChaincodeEndpoint<BurnResponse>('get', "/blockchain/pendingBurnList");
        //console.log('Pending burn list retrieved:', response.data);
        //const burnArray: BurnRequestWithHash[] = response.data.map(item => JSON.parse(item));
        return response.data.burns;
    } catch (error) {
        toast.error('Error fetching pending burn list: '+error);

        console.error('Error fetching pending burn list:', error);
        throw error;
    }
};

/**
 * Fetches the full pending burn transaction JSON string from the backend given its burn request hash.
 *
 * @param {string} burnRequestHash - The burn request hash to query.
 * @returns {Promise<string>} A promise that resolves to the JSON string of the pending burn transaction.
 */
export const fetchPendingBurnTransaction = async (burnRequestHash: string): Promise<string> => {
    try {
        const response = await callChaincodeEndpoint<string>(
            'get',
            "/blockchain/pendingBurnTransaction",
            undefined,
            undefined,
            { burnRequestHash }
        );
        console.log('Pending burn transaction retrieved:', response.data);
        return response.data;
    } catch (error) {
        toast.error('Error fetching pending burn transaction: '+error);

        console.error('Error fetching pending burn transaction:', error);
        throw error;
    }
};

/**
 * Fetches the full finalized burn transaction JSON string from the backend given its burn request hash.
 *
 * @param {string} burnRequestHash - The burn request hash to query.
 * @returns {Promise<string>} A promise that resolves to the JSON string of the finalized burn transaction.
 */
export const fetchFinalizedBurnTransaction = async (burnRequestHash: string): Promise<string> => {
    try {
        const response = await callChaincodeEndpoint<string>(
            'get',
            "/blockchain/finalizedBurnTransaction",
            undefined,
            undefined,
            { burnRequestHash }
        );
        console.log('Finalized burn transaction retrieved:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error fetching finalized burn transaction:', error);
        toast.error('Error fetching finalized burn transaction: '+error);

        throw error;
    }
};

// Fetch blockchain transfer transactions
export const fetchTransferTransactions = async (): Promise<BlockchainTransfer[]> => {
    try {
        const response = await callChaincodeEndpoint<BlockchainTransfer[]>(
            "get",
            "/blockchain/transfer/history",
        );

        return response.data;
    } catch (error) {
        console.error("Error fetching transfer transactions:", error);
        toast.error('Error fetching transfer transactions: '+error);

        throw error;
    }
};

export const sendTransferTransaction = async (to: string, value: number): Promise<void> => {
    try {
        await callChaincodeEndpoint<string>(
            "post",
            "/blockchain/transfer/send",
            "yfw-channel",
            "basic",
            {},
            { params: { to, value } }
        );
    } catch (error) {
        console.error("Error sending transfer:", error);
        toast.error('Error sending transfer: '+error);
        throw error;
    }
};

