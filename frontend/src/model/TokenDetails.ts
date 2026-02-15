export interface TokenDetailsResponse {
    tokenSymbol: string;
    totalSupply: string;
    tokenName: string;
    orgMspIds: string[];
    clientAccountBalance: string;
}

export interface BurnRequest {
    id: string;
    burnWallet: string;
    recipientAccount: string;
    amount: number;
    timestamp: number;
}

export interface BurnRequestWithHash {
    burnRequest: BurnRequest;
    hash: string;
}

export interface BurnResponse {
    burns: BurnRequestWithHash[];
    error?: string;
}
