import axios from 'axios';

const apiBank = axios.create({
    baseURL: 'http://localhost:8081/api',
    //withCredentials: true,
});

apiBank.interceptors.request.use();
apiBank.interceptors.response.use();

export default apiBank;
