export interface TransactionData {
    amount: number;
    fromIBAN: string;
    rawData: {
        additionalInfo: string;
        currency: string;
        BurnRequestHash: string;
    };
    refNumber: string;
    toIBAN: string;
    transferDate: string;
}

export interface TransactionResponse {
    data: TransactionData;
    encryptedHash: string;
    hash: string;
}

export interface ConfirmationWithHashDTO {
    confirmation: TransactionData;
    hash: string;
}

export interface MintResponseDTO {
    mints: ConfirmationWithHashDTO[];
    error: string;
}

export interface BlockchainTransfer{
    from: string;
    to: string;
    value: number;
}