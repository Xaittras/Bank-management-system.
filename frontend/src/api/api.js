// src/api/api.js
import axios from "axios";
const BASE = "/api";

// API instances
export const authApi = axios.create({
    baseURL: "/api/auth"
});

export const userApi = axios.create({
    baseURL: "/api/user"
});

export const adminApi = axios.create({
    baseURL: "/api/admin"
});

// Interceptor
const addInterceptor = (instance) => {

    instance.interceptors.request.use(

        (config) => {

            const token = localStorage.getItem("token");

            config.headers = config.headers || {};

            const url = config.url || "";

            const isAuthRequest =
                url.includes("/login") ||
                url.includes("/register");

            if (token && !isAuthRequest) {
                config.headers.Authorization = `Bearer ${token}`;
            }

            return config;
        },

        (error) => Promise.reject(error)
    );
};

// Apply interceptor
addInterceptor(authApi);
addInterceptor(userApi);
addInterceptor(adminApi);
