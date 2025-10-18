import {TransactionResponse} from "../model/transactions.ts";
import React, {createContext, useCallback, useContext, useState} from "react";
import {getAllTransactions} from "../services/api/bank/transactionsApi.ts";


type BankContextType = {
    transactions: TransactionResponse[];
    fetchTransactions: () => Promise<void>;
    loading: boolean;
    error: string | null;
    fetched: boolean;
}

const BankContext = createContext<BankContextType | undefined>(undefined);

export const BankProvider: React.FC = ({ children }) => {
    const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [fetched, setFetched] = useState(false);


    const fetchTransactions = useCallback(async () => {
        setFetched(() => {
            //if (prevFetched) return prevFetched;

            setLoading(true);
            setError(null);
            getAllTransactions()
                .then((data) => setTransactions(data))
                .catch((error) => setError(error instanceof Error ? error.message : "Unknown error occurred"))
                .finally(() => setLoading(false));

            return true;
        });
    }, []);


    return(
      <BankContext.Provider value={{
          transactions,
          fetchTransactions,
          loading,
          error,
          fetched,
      }}>
          {children}
      </BankContext.Provider>
    );
};

export const useBank = () => {
    const context = useContext(BankContext);
    if(!context){
        throw new Error("useBankContext must be used within a BankProvider")
    }
    return context;
}
