import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { tagApi } from "../../api/tagApi";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { ErrorState } from "../../components/ui/ErrorState";
import { Loading } from "../../components/ui/Loading";
import { Pagination } from "../../components/ui/Pagination";
import { Table } from "../../components/ui/Table";
import { formatDate } from "../../utils/date";
import { TagForm } from "./TagForm";

export function TagsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [editingId, setEditingId] = useState<string | null>(null);

  const tagsQuery = useQuery({
    queryKey: ["tags", page],
    queryFn: () => tagApi.getAll({ page, size: 10, sort: "createdAt,desc" }),
  });

  const createMutation = useMutation({
    mutationFn: tagApi.create,
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["tags"] }),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, request }: { id: string; request: Parameters<typeof tagApi.update>[1] }) =>
      tagApi.update(id, request),
    onSuccess: () => {
      setEditingId(null);
      void queryClient.invalidateQueries({ queryKey: ["tags"] });
      void queryClient.invalidateQueries({ queryKey: ["customers"] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: tagApi.delete,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["tags"] });
      void queryClient.invalidateQueries({ queryKey: ["customers"] });
    },
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-950">Etiketler</h1>
        <p className="mt-1 text-sm text-slate-500">Müşteri etiketlerini oluşturun ve yönetin.</p>
      </div>

      <Card title="Yeni Etiket">
        <TagForm submitLabel="Etiket Oluştur" isSubmitting={createMutation.isPending} onSubmit={(request) => createMutation.mutate(request)} />
        {createMutation.isError ? <div className="mt-4"><ErrorState error={createMutation.error} /></div> : null}
      </Card>

      {tagsQuery.isLoading ? <Loading /> : null}
      {tagsQuery.isError ? <ErrorState error={tagsQuery.error} /> : null}
      {deleteMutation.isError ? <ErrorState error={deleteMutation.error} /> : null}
      {tagsQuery.data?.content.length === 0 ? <EmptyState title="Etiket yok" description="Henüz etiket oluşturulmamış." /> : null}

      {tagsQuery.data && tagsQuery.data.content.length > 0 ? (
        <Card title={`Etiket Listesi (${tagsQuery.data.totalElements})`}>
          <Table headers={["Etiket", "Renk", "Oluşturulma", "Güncelleme", "İşlemler"]}>
            {tagsQuery.data.content.map((tag) => (
              <tr key={tag.id}>
                <td className="px-4 py-3">
                  {editingId === tag.id ? (
                    <TagForm
                      initialTag={tag}
                      submitLabel="Güncelle"
                      isSubmitting={updateMutation.isPending}
                      onSubmit={(request) => updateMutation.mutate({ id: tag.id, request })}
                      onCancel={() => setEditingId(null)}
                    />
                  ) : (
                    <Badge color={tag.color}>{tag.name}</Badge>
                  )}
                </td>
                <td className="px-4 py-3 text-slate-600">{tag.color}</td>
                <td className="px-4 py-3 text-slate-600">{formatDate(tag.createdAt)}</td>
                <td className="px-4 py-3 text-slate-600">{formatDate(tag.updatedAt)}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <Button type="button" variant="secondary" onClick={() => setEditingId(tag.id)}>
                      Düzenle
                    </Button>
                    <Button
                      type="button"
                      variant="danger"
                      disabled={deleteMutation.isPending}
                      onClick={() => {
                        if (window.confirm("Bu etiket silinsin mi?")) {
                          deleteMutation.mutate(tag.id);
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
          {updateMutation.isError ? <div className="mt-4"><ErrorState error={updateMutation.error} /></div> : null}
          <Pagination
            page={tagsQuery.data.page}
            totalPages={tagsQuery.data.totalPages}
            first={tagsQuery.data.first}
            last={tagsQuery.data.last}
            onPageChange={setPage}
          />
        </Card>
      ) : null}
      <p className="text-xs text-slate-500">
        TODO: Admin rol endpointi bu ilk sürümde kullanılmadı: PATCH /api/admin/users/{"{userId}"}/role.
      </p>
    </div>
  );
}
