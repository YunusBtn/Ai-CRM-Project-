import { axiosClient } from "./axiosClient";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

export const authApi = {
  login: async (request: LoginRequest) => {
    const response = await axiosClient.post<AuthResponse>("/api/auth/login", request);
    return response.data;
  },
  register: async (request: RegisterRequest) => {
    const response = await axiosClient.post<AuthResponse>("/api/auth/register", request);
    return response.data;
  },
};
