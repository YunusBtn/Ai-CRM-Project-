import { axiosClient } from "./axiosClient";
import type { PageResponse } from "../types/common";
import type { MessageCreateRequest, MessageListParams, MessageResponse } from "../types/message";

export const messageApi = {
  create: async (conversationId: string, request: MessageCreateRequest) => {
    const response = await axiosClient.post<MessageResponse>(
      `/api/messages/create/${conversationId}`,
      request,
    );
    return response.data;
  },
  getByConversation: async (conversationId: string, params: MessageListParams) => {
    const response = await axiosClient.get<PageResponse<MessageResponse>>(
      `/api/messages/get/${conversationId}`,
      { params },
    );
    return response.data;
  },
};
