import { FormEvent, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { conversationApi } from "../../api/conversationApi";
import { customerApi } from "../../api/customerApi";
import { tagApi } from "../../api/tagApi";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { ErrorState } from "../../components/ui/ErrorState";
import { Loading } from "../../components/ui/Loading";
import { Pagination } from "../../components/ui/Pagination";
import { Select } from "../../components/ui/Select";
import { Table } from "../../components/ui/Table";
import { formatDate } from "../../utils/date";
import { NotesPanel } from "../notes/NotesPanel";
import { CustomerForm } from "./CustomerForm";

export function CustomerDetailPage() {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [conversationPage, setConversationPage] = useState(0);
  const [selectedTagId, setSelectedTagId] = useState("");

  const customerQuery = useQuery({
    queryKey: ["customer", id],
    queryFn: () => customerApi.getById(id || ""),
    enabled: Boolean(id),
  });

  const conversationsQuery = useQuery({
    queryKey: ["conversations", "customer", id, conversationPage],
    queryFn: () =>
      conversationApi.getByCustomer(id || "", {
        page: conversationPage,
        size: 10,
        sort: "createdAt,desc",
      }),
    enabled: Boolean(id),
  });

  const tagsQuery = useQuery({
    queryKey: ["tags", "all-for-customer"],
    queryFn: () => tagApi.getAll({ page: 0, size: 100, sort: "name,asc" }),
  });

  const updateMutation = useMutation({
    mutationFn: (request: Parameters<typeof customerApi.update>[1]) => customerApi.update(id || "", request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["customer", id] });
      void queryClient.invalidateQueries({ queryKey: ["customers"] });
    },
  });

  const addTagMutation = useMutation({
    mutationFn: (tagId: string) => customerApi.addTag(id || "", tagId),
    onSuccess: () => {
      setSelectedTagId("");
      void queryClient.invalidateQueries({ queryKey: ["customer", id] });
      void queryClient.invalidateQueries({ queryKey: ["customers"] });
    },
  });

  const removeTagMutation = useMutation({
    mutationFn: (tagId: string) => customerApi.removeTag(id || "", tagId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["customer", id] });
      void queryClient.invalidateQueries({ queryKey: ["customers"] });
    },
  });

  function handleAddTag(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (selectedTagId) {
      addTagMutation.mutate(selectedTagId);
    }
  }

  if (!id) {
    return <ErrorState error={new Error("Müşteri id bulunamadı.")} />;
  }

  if (customerQuery.isLoading) {
    return <Loading />;
  }

  if (customerQuery.isError) {
    return <ErrorState error={customerQuery.error} />;
  }

  const customer = customerQuery.data;

  return (
    <div className="space-y-6">
      <div>
        <Link className="text-sm font-medium text-brand-700 hover:underline" to="/customers">
          Müşterilere dön
        </Link>
        <h1 className="mt-2 text-2xl font-bold text-slate-950">
          {customer?.firstName} {customer?.lastName}
        </h1>
        <p className="mt-1 text-sm text-slate-500">Müşteri detayları, etiketleri, konuşmaları ve notları</p>
      </div>

      {customer ? (
        <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
          <div className="space-y-6">
            <Card title="Müşteri Bilgileri">
              <CustomerForm
                initialCustomer={customer}
                isSubmitting={updateMutation.isPending}
                onSubmit={(request) => updateMutation.mutate(request)}
              />
              {updateMutation.isError ? <div className="mt-4"><ErrorState error={updateMutation.error} /></div> : null}
            </Card>

            <Card title="Müşteri Konuşmaları">
              {conversationsQuery.isLoading ? <Loading /> : null}
              {conversationsQuery.isError ? <ErrorState error={conversationsQuery.error} /> : null}
              {conversationsQuery.data?.content.length === 0 ? (
                <EmptyState title="Konuşma yok" description="Bu müşteriye ait konuşma bulunmuyor." />
              ) : null}
              {conversationsQuery.data && conversationsQuery.data.content.length > 0 ? (
                <>
                  <Table headers={["Başlık", "Durum", "Atanan", "Son Mesaj", "İşlemler"]}>
                    {conversationsQuery.data.content.map((conversation) => (
                      <tr key={conversation.id}>
                        <td className="px-4 py-3 font-medium text-slate-900">{conversation.title || "Başlıksız"}</td>
                        <td className="px-4 py-3"><Badge>{conversation.status}</Badge></td>
                        <td className="px-4 py-3 text-slate-600">{conversation.assignedToFullName || "-"}</td>
                        <td className="px-4 py-3 text-slate-600">{formatDate(conversation.lastMessageAt)}</td>
                        <td className="px-4 py-3">
                          <Link className="font-medium text-brand-700 hover:underline" to={`/conversations/${conversation.id}`}>
                            Detay
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </Table>
                  <Pagination
                    page={conversationsQuery.data.page}
                    totalPages={conversationsQuery.data.totalPages}
                    first={conversationsQuery.data.first}
                    last={conversationsQuery.data.last}
                    onPageChange={setConversationPage}
                  />
                </>
              ) : null}
            </Card>
          </div>

          <div className="space-y-6">
            <Card title="Etiketler">
              <div className="mb-4 flex flex-wrap gap-2">
                {customer.tags.length > 0 ? (
                  customer.tags.map((tag) => (
                    <span key={tag.id} className="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-sm">
                      <Badge color={tag.color}>{tag.name}</Badge>
                      <button
                        className="text-slate-500 hover:text-red-600"
                        type="button"
                        disabled={removeTagMutation.isPending}
                        onClick={() => removeTagMutation.mutate(tag.id)}
                      >
                        kaldır
                      </button>
                    </span>
                  ))
                ) : (
                  <p className="text-sm text-slate-500">Etiket yok.</p>
                )}
              </div>
              <form className="flex gap-2" onSubmit={handleAddTag}>
                <div className="flex-1">
                  <Select
                    options={[
                      { label: "Etiket seç", value: "" },
                      ...(tagsQuery.data?.content
                        .filter((tag) => !customer.tags.some((customerTag) => customerTag.id === tag.id))
                        .map((tag) => ({ label: tag.name, value: tag.id })) ?? []),
                    ]}
                    value={selectedTagId}
                    onChange={(event) => setSelectedTagId(event.target.value)}
                  />
                </div>
                <Button type="submit" disabled={!selectedTagId || addTagMutation.isPending}>
                  Ekle
                </Button>
              </form>
              {addTagMutation.isError ? <div className="mt-3"><ErrorState error={addTagMutation.error} /></div> : null}
              {removeTagMutation.isError ? <div className="mt-3"><ErrorState error={removeTagMutation.error} /></div> : null}
            </Card>
            <NotesPanel type="customer" ownerId={id} />
          </div>
        </div>
      ) : null}
    </div>
  );
}
