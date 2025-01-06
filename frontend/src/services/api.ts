import axios from "axios";
import { authService } from "./authService";

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
});

// Types
export interface Holding {
  symbol: string;
  proportion: number;
  price: number;
  value: number;
}

export interface FundData {
  date: string;
  fundName: string;
  totalValue: number;
  breakdown: Holding[];
}

export interface PageResponse {
  content: FundData[];
  totalElements: number;
}

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = authService.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      authService.logout();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// API functions
export const getFundBreakdown = async ({
  page = 0,
  size = 10,
  startDate,
  endDate,
  fundName = "",
}: {
  page?: number;
  size?: number;
  startDate?: string;
  endDate?: string;
  fundName?: string;
}): Promise<PageResponse> => {
  const { data } = await api.get("/api/portfolios/funds/breakdown/search", {
    params: { page, size, startDate, endDate, fundName },
  });
  return data;
};

export default api;
