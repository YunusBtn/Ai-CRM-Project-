import type { TagResponse } from "./tag";

export type CustomerStatus = "ACTIVE" | "PASSIVE" | "BLOCKED";

export type CustomerResponse = {
  id: string;
  firstName: string;
  lastName: string;
  phone?: string | null;
  email?: string | null;
  status: CustomerStatus;
  tags: TagResponse[];
  createdAt: string;
  updatedAt: string;
};

export type CustomerCreateRequest = {
  firstName?: string;
  lastName?: string;
  phone?: string;
  email?: string;
};

export type CustomerUpdateRequest = {
  firstName?: string;
  lastName?: string;
  phone?: string;
  email?: string;
  status?: CustomerStatus;
};

export type CustomerListParams = {
  search?: string;
  status?: CustomerStatus | "";
  tagId?: string;
  page?: number;
  size?: number;
  sort?: string;
};
