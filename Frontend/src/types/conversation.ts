export type ConversationStatus = "OPEN" | "CLOSED" | "PENDING" | "ARCHIVED";

export type ConversationResponse = {
  id: string;
  title?: string | null;
  status: ConversationStatus;
  customerId: string;
  customerFullName: string;
  assignedToId?: string | null;
  assignedToFullName?: string | null;
  lastMessageAt?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ConversationCreateRequest = {
  title?: string;
  assignedToId?: string;
};

export type ConversationStatusUpdateRequest = {
  status: ConversationStatus;
};

export type ConversationAssignRequest = {
  assignedToId: string;
};

export type ConversationListParams = {
  status?: ConversationStatus | "";
  assignedToId?: string;
  customerId?: string;
  unassigned?: boolean | "";
  page?: number;
  size?: number;
  sort?: string;
};
