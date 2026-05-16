import type { ConversationStatus } from "./conversation";

export type DashboardSummaryResponse = {
  totalCustomerCount: number;
  activeCustomerCount: number;
  todayCreatedCustomerCount: number;
  openConversationCount: number;
  pendingConversationCount: number;
  waitingReplyConversationCount: number;
  todayInboundMessageCount: number;
  unassignedConversationCount: number;
  todayClosedConversationCount: number;
  myAssignedOpenConversationCount: number;
};

export type ConversationStatusCountResponse = {
  status: ConversationStatus;
  count: number;
};

export type TagCustomerCountResponse = {
  tagId: string;
  tagName: string;
  customerCount: number;
};
