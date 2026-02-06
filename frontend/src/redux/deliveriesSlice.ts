import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {fetchDeliveriesDetails} from "../services/api/backend/blockchain/fetchDeliveriesDetails.ts";
import {Delivery} from "../model/DeliveriesDetails.ts";

interface DeliveriesState {
    deliveries: Delivery[];
    loading: boolean;
    error: string | null;
    fetched: boolean
}

const initialState: DeliveriesState = {
    deliveries: [],
    loading: false,
    error: null,
    fetched: false,
};

export const fetchDeliveries = createAsyncThunk<
    Delivery[],
    void,
    { rejectValue: string }
>('deliveries/fetchDeliveries', async (_, { rejectWithValue }) => {
    try {
        const data = await fetchDeliveriesDetails();
        return data;
    } catch (error) {
        return rejectWithValue(error.message || 'Failed to fetch deliveries');
    }
});

const deliveriesSlice = createSlice({
    name: 'deliveries',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchDeliveries.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchDeliveries.fulfilled, (state, action: PayloadAction<Delivery[]>) => {
                state.deliveries = action.payload;
                state.loading = false;
                state.fetched = true;
            })
            .addCase(fetchDeliveries.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload ?? 'An error occurred';
                state.fetched = false;
            });
    },
});

export default deliveriesSlice.reducer;