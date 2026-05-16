export type MessageDirection = "INBOUND" | "OUTBOUND";
export type SenderType = "CUSTOMER" | "SYSTEM" | "AGENT";

export type MessageResponse = {
  id: string;
  conversationId: string;
  content: string;
  messageDirection: MessageDirection;
  senderType: SenderType;
  senderUserId?: string | null;
  senderUserFullName?: string | null;
  sentAt: string;
  createdAt: string;
  updatedAt: string;
};

export type MessageCreateRequest = {
  content: string;
  messageDirection: MessageDirection;
  senderType: SenderType;
};

export type MessageListParams = {
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
};
