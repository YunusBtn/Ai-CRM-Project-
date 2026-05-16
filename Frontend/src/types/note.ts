export type NoteResponse = {
  id: string;
  customerId?: string | null;
  content: string;
  conversationId?: string | null;
  createdById: string;
  createdByFullName: string;
  createdAt: string;
  updatedAt: string;
};

export type NoteCreateRequest = {
  content: string;
};

export type NoteUpdateRequest = {
  content: string;
};
