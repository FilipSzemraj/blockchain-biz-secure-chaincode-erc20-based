import {DeliveriesResponse, Delivery, DeliveryStatus, PartialDelivery} from "../../../../model/DeliveriesDetails.ts";
import apiMain from "../../../../config/axiosMainConfig.ts";
import {AxiosRequestConfig} from "axios";
import {toast} from "react-toastify";

/**
 * Fetches token details from the blockchain via the backend API.
 * @returns Promise<DeliveriesDetailsResponse> - A promise resolving to a list of deliveries.
 */
export const fetchDeliveriesDetails = async (): Promise<Delivery[]> => {
    try {
        const config: AxiosRequestConfig = {
            params: {
                channelName: "yfw-channel",
                chaincodeName: "basic"
            }
        };

        const response = await apiMain.get<DeliveriesResponse>('/blockchain/queryDeliveries', config);

        //console.log(response.data)
        const parsedData = response.data;

        /*if(response.data.deliveries){
            return [];
        }*/
        // Parse the JSON string
        const { deliveries, error, _links } = parsedData;

        if (error) {
            console.error("DeliveriesResponse Error:", error);
            toast.error('Deliveries response Error: '+error);

            return [] as Delivery[];
        }
        if (!deliveries || !Array.isArray(deliveries)) {
            console.warn("No deliveries array found in response.");
            return [] as Delivery[];
        }

        const deliveriesList: Delivery[] = deliveries.map((item: Delivery) => ({
            deliveryId: item.deliveryId,
            buyerId: item.buyerId,
            sellerId: item.sellerId,
            arbitratorId: item.arbitratorId,
            tokenAmount: Number(item.tokenAmount),
            goodsType: item.goodsType,
            goodsDetails: item.goodsDetails,
            goodsQuantity: Number(item.goodsQuantity),
            goodsDeliveredQuantity: Number(item.goodsDeliveredQuantity),
            goodsSentQuantity: Number(item.goodsSentQuantity),
            currentStatus: item.currentStatus as DeliveryStatus,
            partialDeliveries: (item.partialDeliveries || []).map((pd: PartialDelivery) => ({
                deliveryId: pd.deliveryId,
                goodsType: pd.goodsType,
                goodsDetails: pd.goodsDetails,
                goodsQuantity: Number(pd.goodsQuantity),
                currentStatus: pd.currentStatus as DeliveryStatus,
                creationTimestamp: pd.creationTimestamp,
                updateTimestamp: pd.updateTimestamp,
                expiryTimestamp: pd.expiryTimestamp,
                disputeReason: pd.disputeReason,
                rawData: pd.rawData || {}
            })),
            creationTimestamp: item.creationTimestamp,
            updateTimestamp: item.updateTimestamp,
            expiryTimestamp: item.expiryTimestamp,
            disputeReason: item.disputeReason,
            rawData: item.rawData || {}
        }));

        if (_links) {
            //console.log("HATEOAS links:", _links);
        }

        return deliveriesList;
    } catch (error) {
        toast.error('Error fetching deliveries details: '+error);
        console.error("Error fetching deliveries details:", error);
        throw error;
    }
};
