import React from "react";
import styles from "./Dashboard.module.scss";
import Accordion from "../Accordion/Accordion.tsx";
import Header from "../Header/Header.tsx";
import Sidebar from "../Sidebar/Sidebar.tsx";
import {SidebarProvider} from "../../context/SidebarContext.tsx";
import Wrapper from "../modules/Wrapper.tsx";
import {ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';


const Dashboard: React.FC = () => {

    return(
        <div className={styles.container}>
            <SidebarProvider>
                    <Header />
                    <Sidebar>
                        <Accordion/>
                    </Sidebar>
            </SidebarProvider>

            <Wrapper />
            <ToastContainer />

        </div>
    );
}

export default Dashboard
