import React, { createContext, useContext, useState, ReactNode } from 'react';
import {CertificateDetails} from "../model/CertificateDetails.ts";

interface UserContextType {
    certificateDetails: CertificateDetails;
    setCertificateDetails: (details: CertificateDetails) => void;
    certificateDetailsError: string | null;
    setCertificateDetailsError: (error: string | null) => void;
    tokenDetails: Map<string, string | string[]>;
    setTokenDetails: (details: Map<string, string | string[]>) => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const useUser = () => {
    const context = useContext(UserContext);
    if (!context) {
        throw new Error('useUser must be used within a UserProvider');
    }
    return context;
};

export const UserProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [certificateDetailsError, setCertificateDetailsError] = useState<string | null>('');
    const [certificateDetails, setCertificateDetails] = useState<CertificateDetails>(null);
    const [tokenDetails, setTokenDetails] = useState<Map<string, string | string[]>>(new Map([
        ["tokenSymbol", ""],
        ["totalSupply", ""],
        ["tokenName", ""],
        ["orgMspIds", []],
        ["clientAccountBalance", ""],
    ]));

    return (
        <UserContext.Provider
            value={{
                certificateDetails,
                setCertificateDetails,
                certificateDetailsError,
                setCertificateDetailsError,
                tokenDetails,
                setTokenDetails
            }}>
            {children}
        </UserContext.Provider>
    );
};