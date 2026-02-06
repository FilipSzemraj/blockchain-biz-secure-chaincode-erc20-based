import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {BlockchainTransfer} from "../model/transactions.ts";
import {
    fetchTransferTransactions,
    sendTransferTransaction
} from "../services/api/backend/blockchain/fetchTokenDetails.ts";

interface BlockchainTransfersState {
    blockchainTransfers: BlockchainTransfer[];
    loading: boolean;
    error: string | null;
    fetched: boolean
}

const initialState: BlockchainTransfersState = {
    blockchainTransfers: [],
    loading: false,
    error: null,
    fetched: false,
};

export const  fetchBlockchainTransfers = createAsyncThunk<
    BlockchainTransfer[],
    void,
    { rejectValue: string }
>('blockchainTransfers/fetchBlockchainTransfers', async (_, { rejectWithValue }) => {
    try {
        const data = await fetchTransferTransactions();
        return data;
    } catch (error) {
        return rejectWithValue(error.message || 'Failed to fetch blockchain transfers');
    }
});

export const submitBlockchainTransfer = createAsyncThunk<
    void,
    { to: string; value: number },
    { rejectValue: string }
>('blockchainTransfers/submitTransfer', async ({ to, value}, { rejectWithValue }) => {
    try{
        await sendTransferTransaction(to, value);
    }catch(error){
        return rejectWithValue(error.message || "Transfer failed.");
    }
})

const blockchainTransfersSlice = createSlice({
    name: 'blockchainTransfers',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchBlockchainTransfers.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchBlockchainTransfers.fulfilled, (state, action: PayloadAction<BlockchainTransfer[]>) => {
                state.blockchainTransfers = action.payload;
                state.loading = false;
                state.fetched = true;
            })
            .addCase(fetchBlockchainTransfers.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload ?? 'An error occurred';
                state.fetched = false;
            })
            .addCase(submitBlockchainTransfer.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(submitBlockchainTransfer.fulfilled, (state) => {
                state.loading = false;
                state.fetched = false; // Mark fetched as false to trigger refresh
            })
            .addCase(submitBlockchainTransfer.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload ?? 'Transfer failed.';
            });
    },
});

export default blockchainTransfersSlice.reducer;