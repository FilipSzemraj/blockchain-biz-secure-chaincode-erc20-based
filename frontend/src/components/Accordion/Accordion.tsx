import React, { useState } from "react";
import styles from "./Accordion.module.scss";

const blockchainFunctions = {
    Queries: [
        "getPendingBurnTransaction(burnRequestId)",
        "TokenDetails()",
        "getAllOrgMSPs()",
        "ClientMSPAccountID()",
        "TokenName()",
        "getFinalizedBurnBody(burnRequestId)",
        "getFinalizedBurnTransaction(burnRequestId)",
        "GetTransferHistory()",
        "GetDeliveryById(deliveryId)",
        "getPendingBurnBody(burnRequestId)",
        "getFinalizedBurnList()",
        "BalanceOf(address)",
        "getPublicKey()",
        "TotalSupply()",
        "Decimals()",
        "TokenSymbol()",
        "ClientAccountBalance()",
        "getMintList()",
        "GetAllDeliveries()",
        "getBankIBAN()",
        "ClientAccountID()",
        "getPendingBurnList()"
    ],
    Transactions: [
        "FinalizeBurn(burnRequestId)",
        "Initialize(initialSupply, tokenName, tokenSymbol)",
        "CancelDelivery(deliveryId)",
        "StartDelivery(deliveryId)",
        "TransferFrom(from, to, amount)",
        "AddPartialDelivery(deliveryId, partialDeliveryId, goodsType, goodsQuantity, goodsSentQuantity, timestamp)",
        "Allowance(owner, spender)",
        "Approve(spender, amount)",
        "ConfirmPartialDelivery(deliveryId, partialDeliveryId, acceptedQuantity)",
        "Mint(amount)",
        "CreateDelivery(deliveryId, buyerId, sellerId, arbitratorId, tokenAmount, goodsType, goodsDetails, goodsQuantity, goodsSentQuantity, disputeReason)",
        "ResolvePartialDeliveryDispute(deliveryId, partialDeliveryId, resolutionAmount)",
        "CancelPartialDelivery(deliveryId, partialDeliveryId)",
        "Burn(burnRequestId)",
        "Transfer(to, amount)",
        "ConfirmDelivery(deliveryId)"
    ],
    WalletManagement: [
        "Mint(amount)",
        "Burn(burnRequestId)",
        "FinalizeBurn(burnRequestId)"
    ],
    EventListeners: [
        "listenToEvent(contract, eventName, callback)",
        "stopListeningToEvent(contract, eventName, callback)"
    ]
};
const renderWithNonBreakingSpaces = (text: string) => {
    return text.split(" ").map((word, idx) => {
        if (word.length === 1) {
            return (
                <span key={idx}>
            {word}
                    {String.fromCharCode(160) /* &nbsp; */}
          </span>
            );
        }
        return word + " ";
    });
};

const Accordion = () => {
    const [openCategories, setOpenCategories] = useState<string[]>([]);

    const toggleCategory = (category: string) => {
        setOpenCategories((prev) =>
            prev.includes(category)
                ? prev.filter((item) => item !== category)
                : [...prev, category]
        );
    };

    const openAllCategories = () => {
        setOpenCategories(Object.keys(blockchainFunctions));
    }
    const closeAllCategories = () => {
        setOpenCategories([]);
    };

    return (
        <div className={styles.container}>
            <div>
                <button onClick={openAllCategories} className={styles.container__button}>Expand</button>
                <button onClick={closeAllCategories} className={styles.container__button}>Collapse</button>
            </div>
            {Object.entries(blockchainFunctions).map(([category, functions]) => (
                <div key={category}>
                    <h3
                        onClick={() => toggleCategory(category)}
                        className={styles.container__group}
                    >
                        {category}
                    </h3>
                    {(

                        <ul className={`${styles.container__group__category} ${openCategories.includes(category) ? styles.container__group__category__open : styles.container__group__category__closed}`}>
                            {functions.map((fn, index) => (
                                <li key={index}>{renderWithNonBreakingSpaces(fn)}</li>
                            ))}
                        </ul>
                    )}
                </div>
            ))}
        </div>
    );
};


export default Accordion;
