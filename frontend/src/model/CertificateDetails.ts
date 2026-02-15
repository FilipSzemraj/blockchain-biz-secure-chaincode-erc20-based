export interface CertificateDetails{
    issuer: Map<string, string>;
    validFrom: string | null;
    validTo: string | null;
    subject: Map<string, string>;
    subjectAlternativeNames: string[];
    hfIban: Record<string, string>;
}

export interface CertificateDetailsJson{
    issuer: string | null;
    validFrom: string | null;
    validTo: string | null;
    subject: string | null;
    subjectAlternativeNames: string[];
    hfIban: string | null;
}