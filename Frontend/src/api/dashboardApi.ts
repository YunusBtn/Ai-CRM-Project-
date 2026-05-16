import { axiosClient } from "./axiosClient";
import type {
  ConversationStatusCountResponse,
  DashboardSummaryResponse,
  TagCustomerCountResponse,
} from "../types/dashboard";

export const dashboardApi = {
  getSummary: async () => {
    const response = await axiosClient.get<DashboardSummaryResponse>("/api/dashboard/summary");
    return response.data;
  },
  getConversationStatusDistribution: async () => {
    const response = await axiosClient.get<ConversationStatusCountResponse[]>(
      "/api/dashboard/conversation-status-distribution",
    );
    return response.data;
  },
  getCustomerTagDistribution: async () => {
    const response = await axiosClient.get<TagCustomerCountResponse[]>(
      "/api/dashboard/customer-tag-distribution",
    );
    return response.data;
  },
};
