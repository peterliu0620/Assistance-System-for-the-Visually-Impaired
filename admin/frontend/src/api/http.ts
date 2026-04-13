import axios from 'axios';
import type { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { message } from 'ant-design-vue';
import { clearAuth, getAuthToken } from '@/lib/auth';

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
}

function isApiEnvelope(value: unknown): value is ApiEnvelope<unknown> {
  return typeof value === 'object' && value !== null && 'success' in value;
}

let redirected = false;
const envBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').trim();
const baseURL = envBaseUrl ? envBaseUrl.replace(/\/+$/, '') : '/api';

const instance = axios.create({
  baseURL,
  timeout: 10000
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAuthToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const messageText = error.response?.data?.message || error.message || '网络异常，请稍后重试';

    if (/未登录|过期|失效/.test(messageText)) {
      clearAuth();

      if (!redirected && window.location.pathname !== '/login') {
        redirected = true;
        message.warning('登录状态已失效，请重新登录');
        window.location.href = '/login';
        window.setTimeout(() => {
          redirected = false;
        }, 800);
      }
    }

    return Promise.reject(new Error(messageText));
  }
);

function unwrapResponse<T>(response: AxiosResponse<ApiEnvelope<T> | T>): T {
  const body = response.data;

  if (isApiEnvelope(body)) {
    if (body.success) {
      return body.data as T;
    }

    throw new Error(body.message || '请求失败');
  }

  return body as T;
}

function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return instance.get<ApiEnvelope<T> | T>(url, config).then(unwrapResponse);
}

function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return instance.post<ApiEnvelope<T> | T>(url, data, config).then(unwrapResponse);
}

function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return instance.put<ApiEnvelope<T> | T>(url, data, config).then(unwrapResponse);
}

function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return instance.delete<ApiEnvelope<T> | T>(url, config).then(unwrapResponse);
}

const http = {
  get,
  post,
  put,
  delete: del
};

export default http;
