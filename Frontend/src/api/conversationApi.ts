import { axiosClient } from "./axiosClient";
import type { PageParams, PageResponse } from "../types/common";
import type {
  ConversationAssignRequest,
  ConversationCreateRequest,
  ConversationListParams,
  ConversationResponse,
  ConversationStatusUpdateRequest,
} from "../types/conversation";

export const conversationApi = {
  create: async (customerId: string, request: ConversationCreateRequest) => {
    const response = await axiosClient.post<ConversationResponse>(
      `/api/conversations/create/${customerId}`,
      request,
    );
    return response.data;
  },
  getByCustomer: async (customerId: string, params: PageParams) => {
    const response = await axiosClient.get<PageResponse<ConversationResponse>>(
      `/api/conversations/get/${customerId}`,
      { params },
    );
    return response.data;
  },
  getAll: async (params: ConversationListParams) => {
    const response = await axiosClient.get<PageResponse<ConversationResponse>>(
      "/api/conversations/all",
      { params },
    );
    return response.data;
  },
  getById: async (id: string) => {
    const response = await axiosClient.get<ConversationResponse>(
      `/api/conversations/getConversation/${id}`,
    );
    return response.data;
  },
  delete: async (id: string) => {
    await axiosClient.delete(`/api/conversations/delete/${id}`);
  },
  updateStatus: async (id: string, request: ConversationStatusUpdateRequest) => {
    const response = await axiosClient.put<ConversationResponse>(
      `/api/conversations/updateStatus/${id}`,
      request,
    );
    return response.data;
  },
  assign: async (id: string, request: ConversationAssignRequest) => {
    const response = await axiosClient.patch<ConversationResponse>(
      `/api/conversations/assign/${id}`,
      request,
    );
    return response.data;
  },
  getMy: async (params: PageParams) => {
    const response = await axiosClient.get<PageResponse<ConversationResponse>>(
      "/api/conversations/my",
      { params },
    );
    return response.data;
  },
  getUnassigned: async (params: PageParams) => {
    const response = await axiosClient.get<PageResponse<ConversationResponse>>(
      "/api/conversations/unassigned",
      { params },
    );
    return response.data;
  },
  getWaitingReply: async (params: PageParams) => {
    const response = await axiosClient.get<PageResponse<ConversationResponse>>(
      "/api/conversations/waitingReply",
      { params },
    );
    return response.data;
  },
};
