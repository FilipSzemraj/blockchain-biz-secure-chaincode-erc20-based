import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import {
    fetchTokenDetails,
    fetchTransferTransactions
} from "./fetchTokenDetails.ts";
import {getCertificateInfo} from "../getUserInfo.ts";
import {CertificateDetails} from "../../../../model/CertificateDetails.ts";
import {fetchDeliveries} from "../../../../redux/deliveriesSlice.ts";
import {AppDispatch, store} from "../../../../redux/store.ts";
import {debounce} from "../../../../utils/debounce.tsx";
import {fetchBlockchainTransfers} from "../../../../redux/blockchainTransfersSlice.ts";
import {
    fetchFinalizedBurnTransactions,
    fetchMintTransactions,
    fetchPendingBurnTransactions
} from "../../../../redux/tokenBaseTransactionsSlice.ts";
import {
    fetchTokenDetailsThunk,
    setCertificateDetails,
    setCertificateDetailsError
} from "../../../../redux/userSlice.ts";


const dispatch = store.dispatch as AppDispatch;

const debouncedFetchBlockchainTransfer = debounce(() => dispatch(fetchBlockchainTransfers()), 1000);
const debouncedFetchMintTransactions = debounce(() => dispatch(fetchMintTransactions()), 1000);
const debouncedFetchPendingBurnTransactions = debounce(() => dispatch(fetchPendingBurnTransactions()), 1000);
const debouncedFetchFinalizedBurnTransactions = debounce(() => dispatch(fetchFinalizedBurnTransactions()), 1000);

const debouncedFetchDeliveries = debounce(() => dispatch(fetchDeliveries()), 1000);
const debouncedFetchTokenDetails = debounce(() => dispatch(fetchTokenDetailsThunk()), 1000);

export const initializeEventStream = (): EventSource => {
    const eventSource = new EventSource('http://localhost:8080/api/events/stream');

    eventSource.onopen = () => {
        toast.info("Connected to event stream successfully!");
    };

    eventSource.onmessage = (event) => {
        try {
            const parsed = JSON.parse(event.data);
            const { eventName, _ } = parsed;
            toast.info(`Event Received: ${eventName}`);

            switch (eventName) {
                case 'NewDelivery':
                case 'CancelDelivery':
                case 'FinishedDelivery':
                        debouncedFetchDeliveries();
                        debouncedFetchTokenDetails();
                    break;
                case 'UpdateDelivery':
                    debouncedFetchDeliveries();
                    break;
                case 'DeliveryReadyToComplete':
                    toast.info("Delivery ready to complete!", { autoClose: false });
                    break;
                case 'TransferEvent':
                    debouncedFetchBlockchainTransfer();
                    break;

                case 'MintEvent':
                    debouncedFetchMintTransactions();
                    debouncedFetchTokenDetails();
                    break;


                case 'PendingBurnEvent':
                    debouncedFetchPendingBurnTransactions();
                    debouncedFetchTokenDetails();
                    break;

                case 'FinalizedBurnEvent':
                    debouncedFetchFinalizedBurnTransactions();
                    debouncedFetchTokenDetails();
                    break;

                case 'TokenDetailsUpdated':
                    debouncedFetchTokenDetails();
                    break;

                case 'CertificateUpdated':
                    getCertificateInfo(
                        sessionStorage.getItem("userId") || "",
                        (data) => dispatch(setCertificateDetails(data)),
                        (error) => dispatch(setCertificateDetailsError(error))
                            .then(() => {
                                toast.success("Transactions updated successfully!");
                            })

                    );                    break;

                case 'Transaction':
                    dispatch(fetchBlockchainTransfers())
                        .unwrap()
                        .then(() => {
                            toast.success("Transactions updated successfully!");
                        })
                        .catch((error) => {
                            console.error(error);
                            toast.error("Failed to update transactions.");
                        });

                    dispatch(fetchTokenDetailsThunk())
                        .unwrap()
                        .then(() => {
                            toast.success("Token details updated successfully!");
                        })
                        .catch((error) => {
                            console.error(error);
                            toast.error("Failed to update token details.");
                        });
                    break;
                case 'InitEvent':
                    toast.info("Initialization of chaincode complete!");
                    break;
                default:
                    console.warn('Unhandled event type:', eventName);
                    break;
            }
        } catch (err) {
            console.error('Error parsing event data:', err);
            toast.error('Error parsing event data');
        }
    };

    eventSource.onerror = (error) => {
        console.error('EventSource error:', error);
        toast.error('Event stream error. Reconnecting in 5 seconds.', { autoClose: 5000 });

        eventSource.close();

        setTimeout(() => {
            // Reinitialize the event stream.
            // Make sure to pass any required parameters (like certificateCallbacks) if needed.
            initializeEventStream();
        }, 5000);
    };

    return eventSource;
};
