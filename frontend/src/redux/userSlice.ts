import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {CertificateDetails} from "../model/CertificateDetails.ts";
import {fetchTokenDetails} from "../services/api/backend/blockchain/fetchTokenDetails.ts";

export interface UserState {
    certificateDetails: CertificateDetails | null;
    certificateDetailsError: string | null;
    tokenDetails: Record<string, string | string[]>;
    loading: boolean;
    error: string | null;
}

const initialState: UserState = {
    certificateDetails: null,
    certificateDetailsError: null,
    tokenDetails: {
        tokenSymbol: '',
        totalSupply: '',
        tokenName: '',
        orgMspIds: [],
        clientAccountBalance: '',
    },
    loading: false,
    error: null,
};

export const fetchTokenDetailsThunk = createAsyncThunk<
    Record<string, string | string[]>,
    void,
    { rejectValue: string }
>('user/fetchTokenDetails', async (_, { rejectWithValue }) => {
    try {
        const data = await fetchTokenDetails();
        const plainObject: Record<string, string | string[]> = Object.fromEntries(data);
        return plainObject;
    } catch (error: any) {
        return rejectWithValue(error.message || 'Failed to fetch token details');
    }
});

const userSlice = createSlice({
    name: 'user',
    initialState,
    reducers: {
        setCertificateDetails(state, action: PayloadAction<CertificateDetails>) {
            state.certificateDetails = action.payload;
        },
        setCertificateDetailsError(state, action: PayloadAction<string | null>) {
            state.certificateDetailsError = action.payload;
        },
        setTokenDetails(state, action: PayloadAction<Record<string, string | string[]>>) {
            state.tokenDetails = action.payload;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchTokenDetailsThunk.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(
                fetchTokenDetailsThunk.fulfilled,
                (state, action: PayloadAction<Record<string, string | string[]>>) => {
                    state.tokenDetails = action.payload;
                    state.loading = false;
                }
            )
            .addCase(fetchTokenDetailsThunk.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Error fetching token details';
            });
    },
});

export const { setCertificateDetails, setCertificateDetailsError, setTokenDetails } = userSlice.actions;
export default userSlice.reducer;
