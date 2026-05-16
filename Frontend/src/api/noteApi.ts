import { axiosClient } from "./axiosClient";
import type { PageParams, PageResponse } from "../types/common";
import type { NoteCreateRequest, NoteResponse, NoteUpdateRequest } from "../types/note";

export const noteApi = {
  createForCustomer: async (customerId: string, request: NoteCreateRequest) => {
    const response = await axiosClient.post<NoteResponse>(`/api/notes/create/${customerId}`, request);
    return response.data;
  },
  getByCustomer: async (customerId: string, params: PageParams) => {
    const response = await axiosClient.get<PageResponse<NoteResponse>>(`/api/notes/${customerId}`, {
      params,
    });
    return response.data;
  },
  createForConversation: async (conversationId: string, request: NoteCreateRequest) => {
    const response = await axiosClient.post<NoteResponse>(
      `/api/notes/conversationCreate/${conversationId}`,
      request,
    );
    return response.data;
  },
  getByConversation: async (conversationId: string, params: PageParams) => {
    const response = await axiosClient.get<PageResponse<NoteResponse>>(
      `/api/notes/getConversation/${conversationId}`,
      { params },
    );
    return response.data;
  },
  getById: async (id: string) => {
    const response = await axiosClient.get<NoteResponse>(`/api/notes/get/${id}`);
    return response.data;
  },
  update: async (id: string, request: NoteUpdateRequest) => {
    const response = await axiosClient.put<NoteResponse>(`/api/notes/update/${id}`, request);
    return response.data;
  },
  delete: async (id: string) => {
    await axiosClient.delete(`/api/notes/delete/${id}`);
  },
};
