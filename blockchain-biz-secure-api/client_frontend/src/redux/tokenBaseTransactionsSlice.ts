import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { BurnRequestWithHash } from "../model/TokenDetails.ts";
import { TransactionResponse } from "../model/transactions.ts";
import {
    fetchPendingBurnList,
    fetchFinalizedBurnList,
    fetchMintList
} from "../services/api/backend/blockchain/fetchTokenDetails.ts";

interface TokenBaseTransactionsState {
    mintList: TransactionResponse[];
    pendingBurnList: BurnRequestWithHash[];
    finalizedBurnList: BurnRequestWithHash[];
    loading: boolean;
    error: string | null;
}

const initialState: TokenBaseTransactionsState = {
    mintList: [],
    pendingBurnList: [],
    finalizedBurnList: [],
    loading: false,
    error: null,
};

export const fetchMintTransactions = createAsyncThunk<
    TransactionResponse[],
    void,
    { rejectValue: string }
>('TokenBaseTransactions/fetchMintTransactions', async (_, { rejectWithValue }) => {
    try {
        return await fetchMintList();
    } catch (error) {
        return rejectWithValue(error.message || "Failed to fetch mint transactions.");
    }
});

export const fetchPendingBurnTransactions = createAsyncThunk<
    BurnRequestWithHash[],
    void,
    { rejectValue: string }
>('TokenBaseTransactions/fetchPendingBurnTransactions', async (_, { rejectWithValue }) => {
    try {
        return await fetchPendingBurnList();
    } catch (error) {
        return rejectWithValue(error.message || "Failed to fetch pending burn transactions.");
    }
});

export const fetchFinalizedBurnTransactions = createAsyncThunk<
    BurnRequestWithHash[],
    void,
    { rejectValue: string }
>('TokenBaseTransactions/fetchFinalizedBurnTransactions', async (_, { rejectWithValue }) => {
    try {
        return await fetchFinalizedBurnList();
    } catch (error) {
        return rejectWithValue(error.message || "Failed to fetch finalized burn transactions.");
    }
});

const TokenBaseTransactionsSlice = createSlice({
    name: 'TokenBaseTransactions',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchMintTransactions.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchMintTransactions.fulfilled, (state, action: PayloadAction<TransactionResponse[]>) => {
                state.mintList = action.payload;
                state.loading = false;
            })
            .addCase(fetchMintTransactions.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload ?? "Error fetching mint transactions.";
            })
            .addCase(fetchPendingBurnTransactions.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchPendingBurnTransactions.fulfilled, (state, action: PayloadAction<BurnRequestWithHash[]>) => {
                state.pendingBurnList = action.payload;
                state.loading = false;
            })
            .addCase(fetchPendingBurnTransactions.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload ?? "Error fetching pending burn transactions.";
            })
            .addCase(fetchFinalizedBurnTransactions.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchFinalizedBurnTransactions.fulfilled, (state, action: PayloadAction<BurnRequestWithHash[]>) => {
                state.finalizedBurnList = action.payload;
                state.loading = false;
            })
            .addCase(fetchFinalizedBurnTransactions.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload ?? "Error fetching finalized burn transactions.";
            });
    },
});

export default TokenBaseTransactionsSlice.reducer;
