import React, { useState } from "react";
import styles from "./PrivateKeyUploader.module.scss";


const PrivateKeyUploader: React.FC = () => {
    const [error, setError] = useState<string | null>(null);
    const [privateKey, setPrivateKey] = useState<CryptoKey | null>(null);

    const importPrivateKey = async (pem: string) => {
        try {
            const pemKey = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace(/\s+/g, ""); // Usuń spacje i nowe linie

            const binaryKey = Uint8Array.from(atob(pemKey), (c) => c.charCodeAt(0));

            const importedKey = await window.crypto.subtle.importKey(
                "pkcs8",
                binaryKey.buffer,
                { name: "ECDSA", namedCurve: "P-256" },
                false,
                ["sign"]
            );

            setPrivateKey(importedKey);
            alert("Klucz został zaimportowany!");
        } catch (error) {
            setError("Błąd podczas importowania klucza");
            console.error("Błąd podczas importowania klucza:", error);
            alert("Nie udało się zaimportować klucza.");
        }
    };

    const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = () => {
            const pem = reader.result as string;
            importPrivateKey(pem);
        };
        reader.onerror = () => {
            alert("Nie udało się odczytać pliku.");
        };

        reader.readAsText(file);
    };

    const signMessage = async (message: string) => {
        if (!privateKey) {
            alert("Najpierw zaimportuj klucz!");
            return;
        }

        try {
            const encoder = new TextEncoder();
            const data = encoder.encode(message);

            const signature = await window.crypto.subtle.sign(
                {
                    name: "ECDSA",
                    hash: { name: "SHA-256" },
                },
                privateKey,
                data
            );

            const signatureBase64 = btoa(
                String.fromCharCode(...new Uint8Array(signature))
            );
            console.log("Podpis (Base64):", signatureBase64);
            alert(`Podpis: ${signatureBase64}`);
        } catch (error) {
            setError("Błąd podczas podpisywania wiadomości");
            console.error("Błąd podczas podpisywania wiadomości:", error);
        }
    };

    return (
    <div className={styles.container}>
        <h1>Key Import</h1>
        <div className={styles.container__inputField}>
            <label htmlFor="file-upload" className={styles.container__inputField__custom_file_upload}>
                Upload private key
            </label>
            <input id="file-upload" type="file" accept=".pem" onChange={handleFileUpload}/>
            {privateKey && <p style={{textDecoration:"underline"}}>Key loaded successfully!</p>}
            {error && <p style={{color: "red"}}>{error}</p>}
        </div>
    </div>
    )
        ;
};

export default PrivateKeyUploader;