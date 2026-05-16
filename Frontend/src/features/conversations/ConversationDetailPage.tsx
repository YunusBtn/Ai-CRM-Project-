import { FormEvent, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { conversationApi } from "../../api/conversationApi";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Card } from "../../components/ui/Card";
import { ErrorState } from "../../components/ui/ErrorState";
import { Input } from "../../components/ui/Input";
import { Loading } from "../../components/ui/Loading";
import { Select } from "../../components/ui/Select";
import type { ConversationStatus } from "../../types/conversation";
import { formatDate } from "../../utils/date";
import { MessageList } from "../messages/MessageList";
import { NotesPanel } from "../notes/NotesPanel";

const detailStatusOptions = [
  { label: "OPEN", value: "OPEN" },
  { label: "CLOSED", value: "CLOSED" },
  { label: "PENDING", value: "PENDING" },
  { label: "ARCHIVED", value: "ARCHIVED" },
];

export function ConversationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<ConversationStatus>("OPEN");
  const [assignedToId, setAssignedToId] = useState("");

  const conversationQuery = useQuery({
    queryKey: ["conversation", id],
    queryFn: () => conversationApi.getById(id || ""),
    enabled: Boolean(id),
  });

  const statusMutation = useMutation({
    mutationFn: (nextStatus: ConversationStatus) => conversationApi.updateStatus(id || "", { status: nextStatus }),
    onSuccess: (conversation) => {
      setStatus(conversation.status);
      void queryClient.invalidateQueries({ queryKey: ["conversation", id] });
      void queryClient.invalidateQueries({ queryKey: ["conversations"] });
    },
  });

  const assignMutation = useMutation({
    mutationFn: (targetUserId: string) => conversationApi.assign(id || "", { assignedToId: targetUserId }),
    onSuccess: () => {
      setAssignedToId("");
      void queryClient.invalidateQueries({ queryKey: ["conversation", id] });
      void queryClient.invalidateQueries({ queryKey: ["conversations"] });
    },
  });

  useEffect(() => {
    if (conversationQuery.data?.status) {
      setStatus(conversationQuery.data.status);
    }
  }, [conversationQuery.data?.status]);

  function handleStatusSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    statusMutation.mutate(status);
  }

  function handleAssignSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (assignedToId.trim()) {
      assignMutation.mutate(assignedToId.trim());
    }
  }

  if (!id) {
    return <ErrorState error={new Error("Konuşma id bulunamadı.")} />;
  }

  if (conversationQuery.isLoading) {
    return <Loading />;
  }

  if (conversationQuery.isError) {
    return <ErrorState error={conversationQuery.error} />;
  }

  const conversation = conversationQuery.data;

  return (
    <div className="space-y-6">
      <div>
        <Link className="text-sm font-medium text-brand-700 hover:underline" to="/conversations">
          Konuşmalara dön
        </Link>
        <h1 className="mt-2 text-2xl font-bold text-slate-950">{conversation?.title || "Başlıksız Konuşma"}</h1>
        <p className="mt-1 text-sm text-slate-500">{conversation?.customerFullName}</p>
      </div>

      {conversation ? (
        <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
          <div className="space-y-6">
            <Card title="Konuşma Bilgileri">
              <dl className="grid gap-4 text-sm sm:grid-cols-2">
                <div><dt className="text-slate-500">Durum</dt><dd className="mt-1"><Badge>{conversation.status}</Badge></dd></div>
                <div><dt className="text-slate-500">Müşteri</dt><dd className="mt-1 text-slate-900">{conversation.customerFullName}</dd></div>
                <div><dt className="text-slate-500">Atanan</dt><dd className="mt-1 text-slate-900">{conversation.assignedToFullName || "-"}</dd></div>
                <div><dt className="text-slate-500">Son Mesaj</dt><dd className="mt-1 text-slate-900">{formatDate(conversation.lastMessageAt)}</dd></div>
                <div><dt className="text-slate-500">Oluşturulma</dt><dd className="mt-1 text-slate-900">{formatDate(conversation.createdAt)}</dd></div>
                <div><dt className="text-slate-500">Güncelleme</dt><dd className="mt-1 text-slate-900">{formatDate(conversation.updatedAt)}</dd></div>
              </dl>
            </Card>
            <MessageList conversationId={id} />
          </div>

          <div className="space-y-6">
            <Card title="Durum Güncelle">
              <form className="space-y-3" onSubmit={handleStatusSubmit}>
                <Select
                  value={status}
                  onChange={(event) => setStatus(event.target.value as ConversationStatus)}
                  options={detailStatusOptions}
                />
                <Button type="submit" disabled={statusMutation.isPending}>
                  Güncelle
                </Button>
              </form>
              {statusMutation.isError ? <div className="mt-3"><ErrorState error={statusMutation.error} /></div> : null}
            </Card>

            <Card title="Temsilci Ata">
              <form className="space-y-3" onSubmit={handleAssignSubmit}>
                <Input
                  label="Temsilci UUID"
                  value={assignedToId}
                  onChange={(event) => setAssignedToId(event.target.value)}
                />
                <Button type="submit" disabled={!assignedToId.trim() || assignMutation.isPending}>
                  Ata
                </Button>
              </form>
              {assignMutation.isError ? <div className="mt-3"><ErrorState error={assignMutation.error} /></div> : null}
            </Card>

            <NotesPanel type="conversation" ownerId={id} />
          </div>
        </div>
      ) : null}
    </div>
  );
}
