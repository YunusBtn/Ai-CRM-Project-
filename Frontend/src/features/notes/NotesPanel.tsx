import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { noteApi } from "../../api/noteApi";
import { Button } from "../../components/ui/Button";
import { Card } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { ErrorState } from "../../components/ui/ErrorState";
import { Loading } from "../../components/ui/Loading";
import { Pagination } from "../../components/ui/Pagination";
import { formatDate } from "../../utils/date";
import { NoteForm } from "./NoteForm";

type NotesPanelProps =
  | { type: "customer"; ownerId: string }
  | { type: "conversation"; ownerId: string };

export function NotesPanel({ type, ownerId }: NotesPanelProps) {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [editingId, setEditingId] = useState<string | null>(null);
  const queryKey = ["notes", type, ownerId, page];

  const notesQuery = useQuery({
    queryKey,
    queryFn: () =>
      type === "customer"
        ? noteApi.getByCustomer(ownerId, { page, size: 5, sort: "createdAt,desc" })
        : noteApi.getByConversation(ownerId, { page, size: 5, sort: "createdAt,desc" }),
  });

  const invalidateNotes = () => {
    void queryClient.invalidateQueries({ queryKey: ["notes", type, ownerId] });
  };

  const createMutation = useMutation({
    mutationFn: (content: string) =>
      type === "customer"
        ? noteApi.createForCustomer(ownerId, { content })
        : noteApi.createForConversation(ownerId, { content }),
    onSuccess: invalidateNotes,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, content }: { id: string; content: string }) => noteApi.update(id, { content }),
    onSuccess: () => {
      setEditingId(null);
      invalidateNotes();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: noteApi.delete,
    onSuccess: invalidateNotes,
  });

  return (
    <Card title="Notlar">
      <div className="space-y-5">
        <NoteForm
          submitLabel="Not Ekle"
          isSubmitting={createMutation.isPending}
          onSubmit={(content) => createMutation.mutate(content)}
        />
        {createMutation.isError ? <ErrorState error={createMutation.error} /> : null}
        {notesQuery.isLoading ? <Loading /> : null}
        {notesQuery.isError ? <ErrorState error={notesQuery.error} /> : null}
        {notesQuery.data?.content.length === 0 ? (
          <EmptyState title="Not yok" description="Bu kayıt için henüz not eklenmemiş." />
        ) : null}
        <div className="space-y-3">
          {notesQuery.data?.content.map((note) => (
            <div key={note.id} className="rounded-lg border border-slate-200 p-4">
              {editingId === note.id ? (
                <NoteForm
                  initialContent={note.content}
                  submitLabel="Güncelle"
                  isSubmitting={updateMutation.isPending}
                  onSubmit={(content) => updateMutation.mutate({ id: note.id, content })}
                  onCancel={() => setEditingId(null)}
                />
              ) : (
                <>
                  <p className="whitespace-pre-wrap text-sm text-slate-800">{note.content}</p>
                  <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-xs text-slate-500">
                    <span>
                      {note.createdByFullName} · {formatDate(note.createdAt)}
                    </span>
                    <span className="flex gap-2">
                      <Button type="button" variant="secondary" onClick={() => setEditingId(note.id)}>
                        Düzenle
                      </Button>
                      <Button
                        type="button"
                        variant="danger"
                        disabled={deleteMutation.isPending}
                        onClick={() => {
                          if (window.confirm("Bu not silinsin mi?")) {
                            deleteMutation.mutate(note.id);
                          }
                        }}
                      >
                        Sil
                      </Button>
                    </span>
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
        {updateMutation.isError ? <ErrorState error={updateMutation.error} /> : null}
        {deleteMutation.isError ? <ErrorState error={deleteMutation.error} /> : null}
        {notesQuery.data ? (
          <Pagination
            page={notesQuery.data.page}
            totalPages={notesQuery.data.totalPages}
            first={notesQuery.data.first}
            last={notesQuery.data.last}
            onPageChange={setPage}
          />
        ) : null}
      </div>
    </Card>
  );
}
