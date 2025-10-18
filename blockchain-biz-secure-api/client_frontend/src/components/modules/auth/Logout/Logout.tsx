import React from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../../config/axiosMainConfig.ts';
import styles from "./Logout.module.scss";

const Logout = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            //await api.post('/auth/logout');
        } catch (error) {
            console.error('Error during logout:', error);
        } finally {
            sessionStorage.removeItem('accessToken');
            navigate('/login');
        }
    };

    return (
        <button onClick={handleLogout} className={styles.button}>
            Logout
        </button>
    );
};

export default Logout;
