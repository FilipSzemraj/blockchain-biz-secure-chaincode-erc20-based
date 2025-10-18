import React, {useState, useEffect, useMemo} from "react";
import styles from "./BlockchainTransactions.module.scss";
import ReusableWindow from "../../common/ReusableWindow";
import FormsContainer from "../../common/FormsContainer";

import TransferForm from "./TransferForm.tsx";
import {AppDispatch, RootState} from "../../../../redux/store.ts";
import {useDispatch, useSelector} from "react-redux";
import {fetchBlockchainTransfers, submitBlockchainTransfer} from "../../../../redux/blockchainTransfersSlice.ts";
const BlockchainTransactions: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();

    const [show, setShow] = useState(false);
    const [activeForm, setActiveForm] = useState<"transfer" | "">("");



    const { certificateDetails, tokenDetails } = useSelector(
        (state: RootState) => state.user
    )

    const {blockchainTransfers, loading, error, fetched } = useSelector(
        (state: RootState) => state.blockchainTransfers
    );

    const userOrg = useMemo(() => {
        if (certificateDetails?.issuer) {
            return certificateDetails.issuer.O || "";
        }
        return "";
    }, [certificateDetails?.issuer]);

    const otherOrgs = useMemo(() => {
        const orgMspIds = tokenDetails.orgMspIds;
        if (Array.isArray(orgMspIds)) {
            return orgMspIds.filter(org => org !== userOrg);
        }
        return [];
    }, [tokenDetails, userOrg]);



    useEffect(() => {
        if (show && !fetched) {
            dispatch(fetchBlockchainTransfers());
        }
    }, [show, fetched, dispatch]);


    const handleTransferSubmit = async (to: string, value: number) => {
        dispatch(submitBlockchainTransfer({ to, value }))
            .unwrap()

            .catch((error) => {
                console.error(error);
            });
    };

    const setShowWrapper = (value: boolean) => {
        setShow(value);
        if (!value) {
            setActiveForm("");
        }
    };

    const forms = [
        {
            key: "transfer",
            label: "Transfer",
            component: (props) => (
                <TransferForm {...props} organizations={otherOrgs} onSubmit={handleTransferSubmit} />
            ),
        },

    ];

    return (
        <div className={styles.wrapper}>
            {/* Forms Container */}
            <div
                className={`${styles.wrapper__addContainer} ${
                    activeForm ? styles.wrapper__addContainer__open : ""
                }`}
            >
                <FormsContainer
                    setIsContainerOpen={(value: "transfer" | "") =>
                        setActiveForm(value)
                    }
                    forms={forms}
                    isContainerOpen={activeForm}
                />
            </div>

            <ReusableWindow
                isOpen={show}
                onToggle={setShowWrapper}
                buttonText="Blockchain Transactions"
                refreshFunction={() => dispatch(fetchBlockchainTransfers())}
                classNameWrapper={styles.blockchainWrapper}
                classNameContent={styles.blockchainContent}
                buttons={[
                    {
                        text: "Send Tokens",
                        onClick: () => {
                            setActiveForm(activeForm === "transfer" ? "" : "transfer")
                        },
                        className: styles.test,
                    },
                ]}
            >
                <div className={styles.container}>
                    <div className={styles.list}>
                        <h4><strong>Transfer Transactions:</strong></h4>
                        <ul>
                            {loading ? (
                                <p>Loading transactions...</p>
                            ) : blockchainTransfers.length > 0 ? (
                                blockchainTransfers.map((tx, index) => (
                                    <li key={index} className={styles.list__transactionItem}>
                                        <span>from: {tx.from}, to: {tx.to}, value: {tx.value}</span>
                                    </li>
                                ))
                            ) : (
                                <p>No transactions found.</p>
                            )}
                        </ul>
                        {error && <p className={styles.error}>{error}</p>}
                    </div>
                </div>
            </ReusableWindow>
        </div>
    );
};

export default BlockchainTransactions;
