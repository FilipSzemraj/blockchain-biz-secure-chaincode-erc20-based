import axios, {AxiosRequestConfig} from 'axios';
import {toast} from "react-toastify";

const config: AxiosRequestConfig = {
    withCredentials: true,
};

const apiMain = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true,
});

apiMain.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('accessToken');
    //console.log("token: "+token);
    if (token) {
        config.headers = {
            ...config.headers,
            Authorization: `Bearer ${token}`,
        };
    }
    //console.log(config.headers.Authorization)
    return config;
}, (error) => {
    toast.error("Configuration of connection with server error: "+error);
    return Promise.reject(error);
});

apiMain.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Jeśli serwer zwróci 403 i żądanie jeszcze nie było ponawiane
        if (error.response?.status === 403 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const { data } = await axios.post(
                    '/auth/refresh',
                    {},
                    config
                );

                sessionStorage.setItem('token', data.token);

                originalRequest.headers['Authorization'] = `Bearer ${data.token}`;

                return apiMain(originalRequest);
            } catch (refreshError) {
                toast.error("Token refresh failed: "+refreshError);
                console.error('Token refresh failed:', refreshError);
                sessionStorage.removeItem('token');
                window.location.href = '/login';
            }
        }

        return Promise.reject(error);
    }
);

export default apiMain;
