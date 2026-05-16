import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { messageApi } from "../../api/messageApi";
import { Badge } from "../../components/ui/Badge";
import { Card } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { ErrorState } from "../../components/ui/ErrorState";
import { Input } from "../../components/ui/Input";
import { Loading } from "../../components/ui/Loading";
import { Pagination } from "../../components/ui/Pagination";
import { formatDate } from "../../utils/date";
import { MessageForm } from "./MessageForm";

type MessageListProps = {
  conversationId: string;
};

export function MessageList({ conversationId }: MessageListProps) {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");

  const messagesQuery = useQuery({
    queryKey: ["messages", conversationId, search, page],
    queryFn: () =>
      messageApi.getByConversation(conversationId, {
        search: search.trim(),
        page,
        size: 10,
        sort: "sentAt,desc",
      }),
  });

  const createMutation = useMutation({
    mutationFn: (request: Parameters<typeof messageApi.create>[1]) => messageApi.create(conversationId, request),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["messages", conversationId] }),
  });

  return (
    <Card title="Mesajlar">
      <div className="space-y-5">
        <MessageForm isSubmitting={createMutation.isPending} onSubmit={(request) => createMutation.mutate(request)} />
        {createMutation.isError ? <ErrorState error={createMutation.error} /> : null}
        <Input
          label="Mesaj Ara"
          value={search}
          onChange={(event) => {
            setPage(0);
            setSearch(event.target.value);
          }}
          placeholder="Mesaj içeriğinde ara..."
        />
        {messagesQuery.isLoading ? <Loading /> : null}
        {messagesQuery.isError ? <ErrorState error={messagesQuery.error} /> : null}
        {messagesQuery.data?.content.length === 0 ? (
          <EmptyState title="Mesaj yok" description="Bu konuşmada henüz mesaj bulunmuyor." />
        ) : null}
        <div className="space-y-3">
          {messagesQuery.data?.content.map((message) => (
            <article key={message.id} className="rounded-lg border border-slate-200 p-4">
              <div className="mb-2 flex flex-wrap items-center gap-2">
                <Badge tone={message.messageDirection === "INBOUND" ? "blue" : "green"}>
                  {message.messageDirection}
                </Badge>
                <Badge>{message.senderType}</Badge>
                <span className="text-xs text-slate-500">{formatDate(message.sentAt)}</span>
              </div>
              <p className="whitespace-pre-wrap text-sm text-slate-800">{message.content}</p>
              <p className="mt-2 text-xs text-slate-500">{message.senderUserFullName || "Sistem/Müşteri"}</p>
            </article>
          ))}
        </div>
        {messagesQuery.data ? (
          <Pagination
            page={messagesQuery.data.page}
            totalPages={messagesQuery.data.totalPages}
            first={messagesQuery.data.first}
            last={messagesQuery.data.last}
            onPageChange={setPage}
          />
        ) : null}
      </div>
    </Card>
  );
}
