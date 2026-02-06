import { configureStore } from '@reduxjs/toolkit';
import deliveriesReducer from './deliveriesSlice';
import blockchainTransfersReducer from './blockchainTransfersSlice';
import tokenBaseTransactionsReducer from './tokenBaseTransactionsSlice.ts';
import userReducer from './userSlice';


export const store = configureStore({
    reducer: {
        deliveries: deliveriesReducer,
        blockchainTransfers: blockchainTransfersReducer,
        tokenTransactions: tokenBaseTransactionsReducer,
        user: userReducer,

    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
