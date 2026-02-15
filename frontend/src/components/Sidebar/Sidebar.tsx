import React from 'react';
import styles from './Sidebar.module.scss';
import {useSidebar} from "../../context/SidebarContext.tsx"; // Assume you have this component

const Sidebar: React.FC = ({ children }) => {
    const { isOpen, toggleSidebar } = useSidebar();


    return (
        <>
            <div className={`${styles.sidebar} ${isOpen ? styles.sidebar__open : styles.sidebar}`}>
                <div className={styles.sidebar__content}>
                    <button className={styles.sidebar__closeBtn} onClick={() => toggleSidebar()}>
                        ×
                    </button>
                    {children}
                </div>
            </div>

            {isOpen && <div className={styles.sidebarOverlay} onClick={() => toggleSidebar()} />}
        </>
    );
};

export default Sidebar;
