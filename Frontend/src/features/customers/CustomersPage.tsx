import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { customerApi } from "../../api/customerApi";
import { tagApi } from "../../api/tagApi";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { ErrorState } from "../../components/ui/ErrorState";
import { Input } from "../../components/ui/Input";
import { Loading } from "../../components/ui/Loading";
import { Pagination } from "../../components/ui/Pagination";
import { Select } from "../../components/ui/Select";
import { Table } from "../../components/ui/Table";
import type { CustomerStatus } from "../../types/customer";
import { formatDate } from "../../utils/date";
import { CustomerForm } from "./CustomerForm";

const statusOptions = [
  { label: "Tüm Durumlar", value: "" },
  { label: "Aktif", value: "ACTIVE" },
  { label: "Pasif", value: "PASSIVE" },
  { label: "Engelli", value: "BLOCKED" },
];

function customerStatusTone(status: CustomerStatus) {
  if (status === "ACTIVE") return "green";
  if (status === "BLOCKED") return "red";
  return "slate";
}

export function CustomersPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<CustomerStatus | "">("");
  const [tagId, setTagId] = useState("");

  const params = useMemo(
    () => ({
      search: search.trim(),
      status,
      tagId: tagId.trim(),
      page,
      size: 10,
      sort: "createdAt,desc",
    }),
    [page, search, status, tagId],
  );

  const customersQuery = useQuery({
    queryKey: ["customers", params],
    queryFn: () => customerApi.getAll(params),
  });

  const tagsQuery = useQuery({
    queryKey: ["tags", "filter-options"],
    queryFn: () => tagApi.getAll({ page: 0, size: 100, sort: "name,asc" }),
  });

  const createMutation = useMutation({
    mutationFn: customerApi.create,
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["customers"] }),
  });

  const deleteMutation = useMutation({
    mutationFn: customerApi.delete,
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["customers"] }),
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-950">Müşteriler</h1>
        <p className="mt-1 text-sm text-slate-500">Müşteri kayıtlarını arayın, filtreleyin ve yönetin.</p>
      </div>

      <Card title="Yeni Müşteri">
        <CustomerForm isSubmitting={createMutation.isPending} onSubmit={(request) => createMutation.mutate(request)} />
        {createMutation.isError ? <div className="mt-4"><ErrorState error={createMutation.error} /></div> : null}
      </Card>

      <Card title="Filtreler">
        <div className="grid gap-4 md:grid-cols-4">
          <Input label="Arama" value={search} onChange={(event) => { setPage(0); setSearch(event.target.value); }} placeholder="Ad, email, telefon..." />
          <Select label="Durum" value={status} onChange={(event) => { setPage(0); setStatus(event.target.value as CustomerStatus | ""); }} options={statusOptions} />
          <Select
            label="Etiket"
            value={tagId}
            onChange={(event) => {
              setPage(0);
              setTagId(event.target.value);
            }}
            options={[
              { label: "Tüm Etiketler", value: "" },
              ...(tagsQuery.data?.content.map((tag) => ({ label: tag.name, value: tag.id })) ?? []),
            ]}
          />
        </div>
      </Card>

      {customersQuery.isLoading ? <Loading /> : null}
      {customersQuery.isError ? <ErrorState error={customersQuery.error} /> : null}
      {deleteMutation.isError ? <ErrorState error={deleteMutation.error} /> : null}

      {customersQuery.data?.content.length === 0 ? <EmptyState /> : null}
      {customersQuery.data && customersQuery.data.content.length > 0 ? (
        <Card title={`Müşteri Listesi (${customersQuery.data.totalElements})`}>
          <Table headers={["Ad Soyad", "Email", "Telefon", "Status", "Etiketler", "Oluşturulma", "İşlemler"]}>
            {customersQuery.data.content.map((customer) => (
              <tr key={customer.id}>
                <td className="px-4 py-3 font-medium text-slate-900">
                  {customer.firstName} {customer.lastName}
                </td>
                <td className="px-4 py-3 text-slate-600">{customer.email || "-"}</td>
                <td className="px-4 py-3 text-slate-600">{customer.phone || "-"}</td>
                <td className="px-4 py-3">
                  <Badge tone={customerStatusTone(customer.status)}>{customer.status}</Badge>
                </td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-1">
                    {customer.tags.length > 0 ? customer.tags.map((tag) => <Badge key={tag.id} color={tag.color}>{tag.name}</Badge>) : "-"}
                  </div>
                </td>
                <td className="px-4 py-3 text-slate-600">{formatDate(customer.createdAt)}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <Link
                      className="inline-flex min-h-10 items-center justify-center rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                      to={`/customers/${customer.id}`}
                    >
                      Detay
                    </Link>
                    <Button
                      variant="danger"
                      type="button"
                      disabled={deleteMutation.isPending}
                      onClick={() => {
                        if (window.confirm("Bu müşteri silinsin mi?")) {
                          deleteMutation.mutate(customer.id);
                        }
                      }}
                    >
                      Sil
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </Table>
          <Pagination
            page={customersQuery.data.page}
            totalPages={customersQuery.data.totalPages}
            first={customersQuery.data.first}
            last={customersQuery.data.last}
            onPageChange={setPage}
          />
        </Card>
      ) : null}
    </div>
  );
}
