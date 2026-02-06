import React, {useEffect, useState} from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './LoginForm.module.scss';
import apiMain from "../../../../config/axiosMainConfig.ts";

interface LoginResponse {
    accessToken: string;
    id: number;
}

const LoginForm = () => {
    const [username, setInnerUsername] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [error, setError] = useState<string | null>('');
    const navigate = useNavigate();

    useEffect(() => {
        const checkToken = async () => {
            //console.log("useEffect");
            try {
                await apiMain.post('/auth/verify');
                navigate('/dashboard')
            } catch (err) {
                console.error('Token verification failed', err);
                sessionStorage.removeItem('accessToken');
            }
        };

        const token = sessionStorage.getItem('accessToken');
        if (token) checkToken();
    }, );

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            console.log("username: ", username);
            console.log("password: ", password);

            const response = await axios.post<LoginResponse>('http://localhost:8080/api/auth/login', {
                username,
                password,
            });
            const token : string = response.data.token;
            sessionStorage.setItem('accessToken', token);
            sessionStorage.setItem('username', username);
            sessionStorage.setItem('userId', response.data.id);



            //console.log("token"+token);
            setError(null);

            navigate('/dashboard');
            //alert('Login successful!');
        } catch (err) {
            setError('Invalid credentials'+err);
        }
    };

    return (
        <div className={styles.container}>
            <form onSubmit={handleLogin} className={styles.container__form}>
                <div className={styles['container__form__pair']}>
                    <label>Username:</label>
                    <input
                        type="text"
                        value={username ? username: ""}
                        onChange={(e) => setInnerUsername(e.target.value)}
                    />
                </div>
                <div className={styles['container__form__pair']}>
                    <label>Password:</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button type="submit" className={styles['container__form__button']}>
                    Login
                </button>
                {error && <p className={styles['container__form__error']}>{error}</p>}
            </form>
        </div>
    );
};

export default LoginForm;
