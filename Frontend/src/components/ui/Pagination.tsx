import { Button } from "./Button";

type PaginationProps = {
  page: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  onPageChange: (page: number) => void;
};

export function Pagination({ page, totalPages, first, last, onPageChange }: PaginationProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 pt-4 text-sm text-slate-600">
      <span>
        Sayfa {totalPages === 0 ? 0 : page + 1} / {totalPages}
      </span>
      <div className="flex gap-2">
        <Button variant="secondary" disabled={first} onClick={() => onPageChange(page - 1)}>
          Önceki
        </Button>
        <Button variant="secondary" disabled={last} onClick={() => onPageChange(page + 1)}>
          Sonraki
        </Button>
      </div>
    </div>
  );
}
