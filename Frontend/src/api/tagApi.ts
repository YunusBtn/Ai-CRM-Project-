import { axiosClient } from "./axiosClient";
import type { PageResponse, PageParams } from "../types/common";
import type { TagCreateRequest, TagResponse, TagUpdateRequest } from "../types/tag";

export const tagApi = {
  create: async (request: TagCreateRequest) => {
    const response = await axiosClient.post<TagResponse>("/api/tags/create", request);
    return response.data;
  },
  getById: async (id: string) => {
    const response = await axiosClient.get<TagResponse>(`/api/tags/${id}`);
    return response.data;
  },
  getAll: async (params: PageParams) => {
    const response = await axiosClient.get<PageResponse<TagResponse>>("/api/tags/all", { params });
    return response.data;
  },
  update: async (id: string, request: TagUpdateRequest) => {
    const response = await axiosClient.put<TagResponse>(`/api/tags/${id}`, request);
    return response.data;
  },
  delete: async (id: string) => {
    await axiosClient.delete(`/api/tags/${id}`);
  },
};
