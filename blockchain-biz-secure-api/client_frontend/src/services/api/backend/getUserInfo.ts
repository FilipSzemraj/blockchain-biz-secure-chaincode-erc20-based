import apiMain from "../../../config/axiosMainConfig.ts";
import {CertificateDetails, CertificateDetailsJson} from "../../../model/CertificateDetails.ts";
import {toast} from "react-toastify";

const convertStringToMap = (input: string): Map<string, string> => {
    const map = new Map<string, string>();
    input.split(",").forEach((pair) => {
        const [key, value] = pair.split("=");
        if (key && value) {
            map.set(key.trim(), value.trim());
        }
    });
    return map;
};

const parseHfIban = (hfIban: string): Map<string, string> => {
    try {
        const parsed = JSON.parse(hfIban);
        const attrs = parsed.attrs || {};

        return new Map(
            Object.entries(attrs).map(([key, value]) => [key, String(value)])
        );
    } catch {
        return new Map();
    }
};

export const getCertificateInfo = async (
    id: string,
    setCertificateDetails: (details: CertificateDetails) => void,
    setCertificateDetailsError: (error: string | null) => void
) => {
    try {
        const response = await apiMain.get<CertificateDetailsJson>(`/users/certificate/details/${id}`);

        // Map the API response to the context format
        const mappedDetails: CertificateDetails = {
            issuer: response.data.issuer ? convertStringToMap(response.data.issuer) : new Map(),
            validFrom: response.data.validFrom,
            validTo: response.data.validTo,
            subject: response.data.subject ? convertStringToMap(response.data.subject) : new Map(),
            subjectAlternativeNames: response.data.subjectAlternativeNames || [],
            hfIban: response.data.hfIban ? parseHfIban(response.data.hfIban) : new Map(),
        };

        setCertificateDetails(mappedDetails);
        setCertificateDetailsError(null);
    } catch (error: unknown) {
        if (error.response && error.response.data && error.response.data.message) {
            toast.error("Error: "+error.response.data.message);

            setCertificateDetailsError(error.response.data.message);
        } else {
            toast.error("An unexpected error occurred.");

            setCertificateDetailsError("An unexpected error occurred.");
        }
    }
};