import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { conversationApi } from "../../api/conversationApi";
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
import type { PageResponse } from "../../types/common";
import type { ConversationResponse, ConversationStatus } from "../../types/conversation";
import { formatDate } from "../../utils/date";
import { ConversationForm } from "./ConversationForm";

const statusOptions = [
  { label: "Tüm Durumlar", value: "" },
  { label: "OPEN", value: "OPEN" },
  { label: "CLOSED", value: "CLOSED" },
  { label: "PENDING", value: "PENDING" },
  { label: "ARCHIVED", value: "ARCHIVED" },
];

function statusTone(status: ConversationStatus) {
  if (status === "OPEN") return "green";
  if (status === "PENDING") return "yellow";
  if (status === "CLOSED") return "slate";
  return "red";
}

type ConversationListViewProps = {
  title: string;
  description?: string;
  queryKey: readonly unknown[];
  queryFn: (page: number) => Promise<PageResponse<ConversationResponse>>;
};

export function ConversationListView({ title, description, queryKey, queryFn }: ConversationListViewProps) {
  const [page, setPage] = useState(0);

  const conversationsQuery = useQuery({
    queryKey: [...queryKey, page],
    queryFn: () => queryFn(page),
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-950">{title}</h1>
        {description ? <p className="mt-1 text-sm text-slate-500">{description}</p> : null}
      </div>
      {conversationsQuery.isLoading ? <Loading /> : null}
      {conversationsQuery.isError ? <ErrorState error={conversationsQuery.error} /> : null}
      {conversationsQuery.data?.content.length === 0 ? <EmptyState title="Konuşma yok" /> : null}
      {conversationsQuery.data && conversationsQuery.data.content.length > 0 ? (
        <Card title={`${title} (${conversationsQuery.data.totalElements})`}>
          <ConversationTable conversations={conversationsQuery.data.content} />
          <Pagination
            page={conversationsQuery.data.page}
            totalPages={conversationsQuery.data.totalPages}
            first={conversationsQuery.data.first}
            last={conversationsQuery.data.last}
            onPageChange={setPage}
          />
        </Card>
      ) : null}
    </div>
  );
}

function ConversationTable({ conversations }: { conversations: ConversationResponse[] }) {
  return (
    <Table headers={["Başlık", "Durum", "Müşteri", "Atanan Temsilci", "Son Mesaj Zamanı", "Oluşturulma", "İşlemler"]}>
      {conversations.map((conversation) => (
        <tr key={conversation.id}>
          <td className="px-4 py-3 font-medium text-slate-900">{conversation.title || "Başlıksız"}</td>
          <td className="px-4 py-3">
            <Badge tone={statusTone(conversation.status)}>{conversation.status}</Badge>
          </td>
          <td className="px-4 py-3 text-slate-600">{conversation.customerFullName}</td>
          <td className="px-4 py-3 text-slate-600">{conversation.assignedToFullName || "-"}</td>
          <td className="px-4 py-3 text-slate-600">{formatDate(conversation.lastMessageAt)}</td>
          <td className="px-4 py-3 text-slate-600">{formatDate(conversation.createdAt)}</td>
          <td className="px-4 py-3">
            <Link className="font-medium text-brand-700 hover:underline" to={`/conversations/${conversation.id}`}>
              Detay
            </Link>
          </td>
        </tr>
      ))}
    </Table>
  );
}

export function ConversationsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState<ConversationStatus | "">("");
  const [assignedToId, setAssignedToId] = useState("");
  const [customerId, setCustomerId] = useState("");
  const [unassigned, setUnassigned] = useState<"" | "true" | "false">("");

  const params = useMemo(
    () => ({
      status,
      assignedToId: assignedToId.trim(),
      customerId: customerId.trim(),
      unassigned: unassigned === "" ? "" : unassigned === "true",
      page,
      size: 10,
      sort: "createdAt,desc",
    }),
    [assignedToId, customerId, page, status, unassigned],
  );

  const conversationsQuery = useQuery({
    queryKey: ["conversations", "all", params],
    queryFn: () => conversationApi.getAll(params),
  });

  const createMutation = useMutation({
    mutationFn: ({ customerId: targetCustomerId, request }: { customerId: string; request: Parameters<typeof conversationApi.create>[1] }) =>
      conversationApi.create(targetCustomerId, request),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["conversations"] }),
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-950">Konuşmalar</h1>
        <p className="mt-1 text-sm text-slate-500">Konuşmaları filtreleyin ve detaylarına erişin.</p>
      </div>

      <Card title="Yeni Konuşma">
        <ConversationForm
          isSubmitting={createMutation.isPending}
          onSubmit={(targetCustomerId, request) => createMutation.mutate({ customerId: targetCustomerId, request })}
        />
        {createMutation.isError ? <div className="mt-4"><ErrorState error={createMutation.error} /></div> : null}
      </Card>

      <Card title="Filtreler">
        <div className="grid gap-4 md:grid-cols-4">
          <Select
            label="Durum"
            value={status}
            onChange={(event) => {
              setPage(0);
              setStatus(event.target.value as ConversationStatus | "");
            }}
            options={statusOptions}
          />
          <Input
            label="Atanan Temsilci UUID"
            value={assignedToId}
            onChange={(event) => {
              setPage(0);
              setAssignedToId(event.target.value);
            }}
          />
          <Input
            label="Müşteri UUID"
            value={customerId}
            onChange={(event) => {
              setPage(0);
              setCustomerId(event.target.value);
            }}
          />
          <Select
            label="Atanmamış"
            value={unassigned}
            onChange={(event) => {
              setPage(0);
              setUnassigned(event.target.value as "" | "true" | "false");
            }}
            options={[
              { label: "Tümü", value: "" },
              { label: "Evet", value: "true" },
              { label: "Hayır", value: "false" },
            ]}
          />
        </div>
      </Card>

      {conversationsQuery.isLoading ? <Loading /> : null}
      {conversationsQuery.isError ? <ErrorState error={conversationsQuery.error} /> : null}
      {conversationsQuery.data?.content.length === 0 ? <EmptyState title="Konuşma yok" /> : null}
      {conversationsQuery.data && conversationsQuery.data.content.length > 0 ? (
        <Card title={`Konuşma Listesi (${conversationsQuery.data.totalElements})`}>
          <ConversationTable conversations={conversationsQuery.data.content} />
          <Pagination
            page={conversationsQuery.data.page}
            totalPages={conversationsQuery.data.totalPages}
            first={conversationsQuery.data.first}
            last={conversationsQuery.data.last}
            onPageChange={setPage}
          />
        </Card>
      ) : null}
    </div>
  );
}
