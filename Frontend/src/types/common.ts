export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type SortDirection = "asc" | "desc";

export type PageParams = {
  page?: number;
  size?: number;
  sort?: string;
};

export type ApiErrorResponse = {
  message?: string;
  error?: string;
  path?: string;
  status?: number;
  timestamp?: string;
};
