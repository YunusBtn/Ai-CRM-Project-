export type TagResponse = {
  id: string;
  name: string;
  color: string;
  createdAt: string;
  updatedAt: string;
};

export type TagCreateRequest = {
  name: string;
  color?: string;
};

export type TagUpdateRequest = {
  name?: string;
  color?: string;
};
