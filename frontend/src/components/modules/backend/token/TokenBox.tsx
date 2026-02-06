import React, { useEffect, useState } from "react";
import styles from "./TokenBox.module.scss";
import ReusableWindow from "../../common/ReusableWindow";
import { useBank } from "../../../../context/BankContext";
import FormsContainer from "../../common/FormsContainer";
import MintForm from "./MintForm";
import BurnForm from "./BurnForm";
import FinalizeBurnForm from "./FinalizeBurnForm";
import ToggleTransactionDetails from "../../common/ToggleTransactionDetails.tsx";
import {useDispatch, useSelector} from "react-redux";
import { RootState, AppDispatch } from "../../../../redux/store.ts";
import {
    fetchFinalizedBurnTransactions,
    fetchMintTransactions,
    fetchPendingBurnTransactions
} from "../../../../redux/tokenBaseTransactionsSlice.ts";
import {TransactionResponse} from "../../../../model/transactions.ts";



const TokenBox: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();

    const [show, setShow] = useState(false);

    const [activeForm, setActiveForm] = useState<"mint" | "burn" | "finalizeBurn" | "">("");
    const { transactions, fetchTransactions } = useBank();
    const { mintList, pendingBurnList, finalizedBurnList, loading, error } = useSelector(
        (state: RootState) => state.tokenTransactions
    );

    const {certificateDetails} = useSelector(
        (state: RootState) => state.user
    )


    const [dummy, setDummy] = useState(0);
    const forceUpdate = () => {
        setDummy((prev) => prev + 1)};

    const combinedFetchedList: string[] = [
        ...mintList.map(tx => tx.hash.toString()),
        ...pendingBurnList.map(tx => tx.hash.toString()),
        ...finalizedBurnList.map(tx => tx.hash.toString())];
    const unprocessedTransactions = transactions.filter((tx) => {
        return !combinedFetchedList.some((combined) => tx.hash.trim() === combined.trim());
    });

    const finalizeBurnList: TransactionResponse[] = transactions.filter(transaction => {
        const burnRequestHash = transaction.data.rawData.BurnRequestHash;

        return pendingBurnList.some(pendingBurn => pendingBurn.hash === burnRequestHash);
    });

    const [mintListForUserOrg, setMintListForUserOrg] = useState<TransactionResponse[]>([]);



    const [filtered, setFiltered] = useState<boolean>(false);




    useEffect(() => {
        if (!certificateDetails || !mintList) return;

        const userHfIban = certificateDetails.hfIban?.["hf.iban"];
        //console.log(userHfIban);

        if (filtered && userHfIban) {
            setMintListForUserOrg(mintList.filter((mint) => {
                console.log("mint.from"+mint.data.fromIBAN);
                console.log("mint equal"+mint.data.fromIBAN === userHfIban);
                return mint.data.fromIBAN === userHfIban
            }));
        } else {
            setMintListForUserOrg(mintList);
        }
    }, [mintList, certificateDetails, filtered]);

    /**
     * Toggles the main window. When closed, also reset active form.
     * @param value - true to open, false to close.
     */
    const setShowWrapper = (value: boolean) => {
        setShow(value);
        if (!value) {
            setActiveForm("");
        }
    };


    useEffect(() => {
        if (transactions.length === 0) {
            fetchTransactions();
        }
        dispatch(fetchMintTransactions());
        dispatch(fetchPendingBurnTransactions());
        dispatch(fetchFinalizedBurnTransactions());
    }, [dispatch]);

    const forms = [
        {
            key: "mint",
            label: "Create New Mint",
            component: (props) => (
                <MintForm
                    {...props}
                    transactions={transactions}
                    availableTransactions={unprocessedTransactions}
                />
            ),
        },
        {
            key: "burn",
            label: "Create New Burn",
            component: (props) => (
                <BurnForm {...props} transactions={transactions} />
            ),
        },
        {
            key: "finalizeBurn",
            label: "Finalize Burn",
            component: (props) => (
                <FinalizeBurnForm {...props} availableBurnRequests={finalizeBurnList} />
            ),
        },
    ];

    return (
        <div className={styles.wrapper}>
            <div
                className={`${styles.wrapper__addContainer} ${
                    activeForm ? styles.wrapper__addContainer__open : ""
                }`}
            >
                <FormsContainer
                    setIsContainerOpen={(value: "mint" | "burn" | "finalizeBurn" | "") => setActiveForm(value)}
                    forms={forms}
                    isContainerOpen={activeForm}
                />
            </div>

            <ReusableWindow
                isOpen={show}
                onToggle={setShowWrapper}
                buttonText="Token"
                refreshFunction={() => {
                    dispatch(fetchMintTransactions());
                    dispatch(fetchPendingBurnTransactions());
                    dispatch(fetchFinalizedBurnTransactions());
                }}
                classNameWrapper={styles.tokenWrapper}
                classNameContent={styles.tokenContent}
                buttons={[
                    {
                        text: "Mint",
                        onClick: () =>
                            setActiveForm(activeForm === "mint" ? "" : "mint"),
                        className: styles.test,
                    },
                    {
                        text: "Burn",
                        onClick: () =>
                            setActiveForm(activeForm === "burn" ? "" : "burn"),
                        className: styles.test,
                    },
                    {
                        text: "Finalize Burn",
                        onClick: () =>
                            setActiveForm(
                                activeForm === "finalizeBurn" ? "" : "finalizeBurn"
                            ),
                        className: styles.test,
                    },
                ]}
            >
                <div className={styles.container}>
                    <div className={styles.container__header}>
                        <label>
                            <input
                                type="checkbox"
                                checked={filtered}
                                onChange={() => setFiltered(prev => !prev)}
                            />
                            Show only transactions for my IBAN.
                        </label>
                    </div>

                    {/* Section for Minted Transactions */}
                    <div className={styles.list}>
                        <h4><strong>Processed operations (Minted):</strong></h4>
                        <ul>
                            {mintListForUserOrg.length > 0 ? (
                                mintListForUserOrg.map((mint, index) => (
                                    <li key={index} className={styles.list__transactionItem}>
                                        <ToggleTransactionDetails
                                            transaction={mint}
                                            onToggle={forceUpdate}
                                            defaultExpanded={index === 0}/>
                                        - <strong>ref:</strong>{" "}
                                        {transactions.find(
                                            (tx) => tx?.hash?.trim() === mint.hash.trim()
                                        )?.data.refNumber || "equivalent bank transfer not found"}
                                    </li>
                                ))
                            ) : (
                                <>
                                    <p>No mint transactions.</p>
                                    {error && <p>{error}</p>}
                                </>
                            )}
                        </ul>
                    </div>

                    <div className={styles.list}>
                        <h4><strong>Pending burn transactions:</strong></h4>
                        <ul>
                            {pendingBurnList.length > 0 ? (
                                pendingBurnList.map((pending, index) => (
                                    <li key={index} className={styles.list__transactionItem}>
                                        <ToggleTransactionDetails
                                            transaction={pending}
                                            onToggle={forceUpdate}
                                            defaultExpanded={index === 0}/>
                                        - <strong>ref: </strong>{" "}
                                        {transactions.find(
                                            (tx) => tx?.data?.rawData?.BurnRequestHash?.trim() === pending.hash.trim()
                                        )?.data.refNumber || "equivalent bank transfer not found"}
                                    </li>
                                ))
                            ) : (
                                <p>No pending burn transactions.</p>
                            )}
                        </ul>
                    </div>

                    <div className={styles.list}>
                        <h4><strong>Finalized burn transactions:</strong></h4>
                        <ul>
                            {finalizedBurnList.length > 0 ? (
                                finalizedBurnList.map((finalized, index) => (
                                    <li key={index} className={styles.list__transactionItem}>
                                        <ToggleTransactionDetails
                                            transaction={finalized}
                                            onToggle={forceUpdate}
                                            defaultExpanded={index === 0}/>
                                        - <strong>ref: </strong>{" "}
                                        {transactions.find(
                                            (tx) => tx?.data?.rawData?.BurnRequestHash?.trim() === finalized.hash.trim()
                                        )?.data.refNumber || "equivalent bank transfer not found"}
                                    </li>
                                ))
                            ) : (
                                <p>No finalized burn transactions.</p>
                            )}
                        </ul>
                    </div>
                </div>
            </ReusableWindow>
        </div>
    );
};

export default TokenBox;
