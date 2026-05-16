import { axiosClient } from "./axiosClient";
import type { PageResponse } from "../types/common";
import type {
  CustomerCreateRequest,
  CustomerListParams,
  CustomerResponse,
  CustomerUpdateRequest,
} from "../types/customer";

export const customerApi = {
  create: async (request: CustomerCreateRequest) => {
    const response = await axiosClient.post<CustomerResponse>("/api/customers/create", request);
    return response.data;
  },
  getById: async (id: string) => {
    const response = await axiosClient.get<CustomerResponse>(`/api/customers/${id}`);
    return response.data;
  },
  getAll: async (params: CustomerListParams) => {
    const response = await axiosClient.get<PageResponse<CustomerResponse>>("/api/customers/all", {
      params,
    });
    return response.data;
  },
  update: async (id: string, request: CustomerUpdateRequest) => {
    const response = await axiosClient.put<CustomerResponse>(`/api/customers/${id}`, request);
    return response.data;
  },
  delete: async (id: string) => {
    await axiosClient.delete(`/api/customers/${id}`);
  },
  addTag: async (customerId: string, tagId: string) => {
    const response = await axiosClient.post<CustomerResponse>(
      `/api/customers/${customerId}/tags/${tagId}`,
    );
    return response.data;
  },
  removeTag: async (customerId: string, tagId: string) => {
    const response = await axiosClient.delete<CustomerResponse>(
      `/api/customers/${customerId}/tags/${tagId}`,
    );
    return response.data;
  },
};
