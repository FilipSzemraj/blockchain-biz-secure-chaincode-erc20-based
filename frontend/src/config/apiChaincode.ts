import apiMain from './axiosMainConfig';
import { AxiosRequestConfig, AxiosResponse } from 'axios';
import {toast} from "react-toastify";

/**
 * A helper function to call endpoints that require channelName and chaincodeName.
 *
 * @param method - HTTP method ('get', 'post', etc.).
 * @param endpoint - API endpoint path (e.g., '/blockchain/mintList').
 * @param channelName - The channel name to attach as a query parameter (default: 'defaultChannel').
 * @param chaincodeName - The chaincode name to attach as a query parameter (default: 'defaultChaincode').
 * @param data - Optional data to send with the request (for POST, PUT, etc.).
 * @param config - Additional Axios config if needed.
 * @returns A promise resolving to the Axios response.
 */
export async function callChaincodeEndpoint<T>(
    method: 'get' | 'post' | 'put' | 'delete',
    endpoint: string,
    channelName: string = 'yfw-channel',
    chaincodeName: string = 'basic',
    data: object = {},
    config: AxiosRequestConfig = {}
): Promise<AxiosResponse<T>> {
    const requestConfig: AxiosRequestConfig = {
        ...config,
        params: {
            channelName,
            chaincodeName,
            ...(config.params || {})
        }
    };
    switch (method) {
        case 'get':
            return apiMain.get<T>(endpoint, requestConfig);
        case 'post':
            return apiMain.post<T>(endpoint, data, requestConfig);
        case 'put':
            return apiMain.put<T>(endpoint, data, requestConfig);
        case 'delete':
            return apiMain.delete<T>(endpoint, requestConfig);
        default:
            toast.error(`Unsupported method: ${method}`);
            throw new Error(`Unsupported method: ${method}`);
    }
}
