import Logout from "../modules/auth/Logout/Logout.tsx";
import React, {useEffect, useState} from "react";
import {getCertificateInfo} from "../../services/api/backend/getUserInfo.ts";
import styles from "./Header.module.scss";
import {useSidebar} from "../../context/SidebarContext.tsx";
import Hamburger from "../Sidebar/Hamburger.tsx";
import BackendUI from "../modules/backend/BackendUI.tsx";
import {initializeEventStream} from "../../services/api/backend/blockchain/eventStream.ts";
import {AppDispatch, RootState} from "../../redux/store.ts";
import {useDispatch, useSelector} from "react-redux";
import {setCertificateDetails, setCertificateDetailsError} from "../../redux/userSlice.ts";

const Header: React.FC = () => {
    const { isOpen, toggleSidebar } = useSidebar();
    const [username, setUsername] = useState<string>("");
    const dispatch = useDispatch<AppDispatch>();

    const { certificateDetails, certificateDetailsError } = useSelector((state: RootState) => state.user);


    useEffect(() => {
        const usernameSession = sessionStorage.getItem("username");
        let eventSource: EventSource | null = null;

        if (usernameSession) {
            setUsername(usernameSession);
        }

        const userId = sessionStorage.getItem("userId");

        if (userId) {
            getCertificateInfo(
                userId,
                (data) => {
                    const plainObject = {
                        ...data,
                        subject: Object.fromEntries(data.subject),
                        hfIban: Object.fromEntries(data.hfIban),
                        issuer: Object.fromEntries(data.issuer),
                    };
                    dispatch(setCertificateDetails(plainObject));
                },
                (error) => dispatch(setCertificateDetailsError(error))
            );

            eventSource = initializeEventStream();
        }

        return () => {
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [dispatch]);



    const isCertificateDetailsAvailable = certificateDetails && certificateDetails.issuer && certificateDetails.hfIban;

    return (
        <div className={`${styles.header} ${isOpen ? styles.header__collapsed : styles.header__expanded}`}>
            <div className={styles.header__userInfoSection}>
                <h1>Dashboard</h1>
                <h2>{username || "Unknown user"}</h2>
                <h2>
                    {isCertificateDetailsAvailable && Object.keys(certificateDetails.issuer).length > 0 && certificateDetails.issuer["O"] ? (
                        certificateDetails.issuer["O"]
                    ) : (
                        <>
                            <div>Certificate details</div>
                            <div>Error: {certificateDetailsError || "No certificate details available"}</div>
                        </>
                    )}
                </h2>
                <h2>
                    {isCertificateDetailsAvailable && Object.keys(certificateDetails.hfIban).length > 0 ? (
                        certificateDetails.hfIban["hf.iban"] || "Unknown IBAN"
                    ) : (
                        "No IBAN available"
                    )}
                </h2>
            </div>
            <div className={`${styles.header__hamburger} ${isOpen ? styles.header__hamburger__closed : ''}`}>
                <Hamburger onClick={() => toggleSidebar()}/>
            </div>
            <div className={styles.header__rightSideWrapper}>
                <div className={styles.header__logout}>
                    <Logout/>
                </div>
                <div className={styles.header__backendUI}>
                    <BackendUI />
                </div>
            </div>
        </div>
    );
}
export default Header;
