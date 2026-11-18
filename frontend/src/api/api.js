// src/api/api.js
import axios from "axios";

export const authApi = axios.create({ baseURL: "/api/auth" });
export const userApi = axios.create({ baseURL: "/api/user" });
export const adminApi = axios.create({ baseURL: "/api/admin" });

// Ключ ідемпотентності: crypto.randomUUID доступний лише в secure context (https/localhost)
export const newIdempotencyKey = () =>
  typeof crypto !== "undefined" && crypto.randomUUID
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(36).slice(2)}-${Math.random().toString(36).slice(2)}`;

// Текст помилки з відповіді сервера (рядок) або запасний варіант
export const errorMessage = (err, fallback) => {
  const data = err?.response?.data;
  return typeof data === "string" && data ? data : fallback;
};

const addInterceptors = (instance) => {
  instance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("token");
      const url = config.url || "";
      const isAuthRequest = url.includes("/login") || url.includes("/register");
      config.headers = config.headers || {};
      if (token && !isAuthRequest) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Протермінований/невалідний токен: вихід на сторінку входу
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      const url = error.config?.url || "";
      const isAuthRequest = url.includes("/login") || url.includes("/register");
      if (error.response?.status === 401 && !isAuthRequest) {
        localStorage.removeItem("token");
        window.location.assign("/");
      }
      return Promise.reject(error);
    }
  );
};

addInterceptors(authApi);
addInterceptors(userApi);
addInterceptors(adminApi);
