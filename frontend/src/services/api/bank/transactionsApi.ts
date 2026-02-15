import apiBank from '../../../config/axiosBankConfig.ts';
import { AxiosError, AxiosRequestConfig } from "axios";
import {TransactionResponse} from "../../../model/transactions.ts";
import {toast} from "react-toastify";

/**
 * Pobiera wszystkie transakcje.
 * @returns Promise z listą transakcji
 */
export const getAllTransactions = async (): Promise<TransactionResponse[]> => {
    try {
        const response = await apiBank.get<TransactionResponse[]>('/transactions');

        //console.log("zaraz zwroce:");
        return Array.isArray(response.data)
            ? response.data.map(item => typeof item === 'string' ? JSON.parse(item) : item)
            : [response.data];
    } catch (error) {
        if (error instanceof AxiosError) {
            toast.error(`Failed to retrieve transactions: ${error.response?.data?.message || error.message}`);
            throw new Error(`Failed to retrieve transactions: ${error.response?.data?.message || error.message}`);
        }
        throw new Error('Unknown error occurred while fetching transactions');
    }
};

/**
 * Pobiera transakcję na podstawie identyfikatora.
 * @param id - Identyfikator transakcji
 * @returns Promise z danymi transakcji
 */
export const getTransactionById = async (id: string): Promise<TransactionResponse> => {
    if (!id) {
        throw new Error('Query parameter "id" is required.');
    }

    try {
        const config: AxiosRequestConfig = {
            params: { id },
            responseType: 'json'
        };

        const response = await apiBank.get<TransactionResponse>(`/transactions/transactionsById`, config);

        return typeof response.data === 'string'
            ? JSON.parse(response.data)
            : response.data;
    } catch (error) {
        if (error instanceof AxiosError) {
            toast.error(`Failed to retrieve transaction with id ${id}` +error);
            throw new Error(
                `Failed to retrieve transaction with id ${id}: ${error.response?.data?.message || error.message}`
            );
        }
        throw new Error('Unknown error occurred while fetching transaction');
    }
};