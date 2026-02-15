export enum DeliveryStatus {
    CREATED = "CREATED",
    IN_TRANSIT = "IN_TRANSIT",
    DELIVERED = "DELIVERED",
    COMPLETED = "COMPLETED",
    DISPUTE = "DISPUTE",
    DISPUTE_RESOLVED = "DISPUTE_RESOLVED",
    CANCELLED = "CANCELLED"
}

export interface PartialDelivery {
    deliveryId: string;
    goodsType: string;
    goodsDetails: string;
    goodsQuantity: number;
    currentStatus: DeliveryStatus;
    creationTimestamp: string;
    updateTimestamp: string;
    expiryTimestamp: string;
    disputeReason?: string;
    rawData?: Record<string, unknown>;
}

export interface Delivery {
    deliveryId: string;
    buyerId: string;
    sellerId: string;
    arbitratorId: string;
    tokenAmount: number;
    goodsType: string;
    goodsDetails: string;
    goodsQuantity: number;
    goodsDeliveredQuantity: number;
    goodsSentQuantity: number;
    currentStatus: DeliveryStatus;
    partialDeliveries: PartialDelivery[];
    creationTimestamp: string;
    updateTimestamp: string;
    expiryTimestamp: string;
    disputeReason?: string;
    rawData?: Record<string, unknown>;
}

export interface DeliveriesResponse {
    deliveries: Delivery[];
    error?: string | null;
    _links?: Record<string, unknown>;
}