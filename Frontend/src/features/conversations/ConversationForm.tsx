import { FormEvent, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import type { ConversationCreateRequest } from "../../types/conversation";

type ConversationFormProps = {
  isSubmitting?: boolean;
  onSubmit: (customerId: string, request: ConversationCreateRequest) => void;
};

export function ConversationForm({ isSubmitting = false, onSubmit }: ConversationFormProps) {
  const [customerId, setCustomerId] = useState("");
  const [title, setTitle] = useState("");
  const [assignedToId, setAssignedToId] = useState("");
  const [error, setError] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    if (!customerId.trim()) {
      setError("Müşteri UUID zorunludur.");
      return;
    }

    onSubmit(customerId.trim(), {
      title: title.trim() || undefined,
      assignedToId: assignedToId.trim() || undefined,
    });
    setTitle("");
    setAssignedToId("");
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <div className="grid gap-4 md:grid-cols-3">
        <Input label="Müşteri UUID" value={customerId} onChange={(event) => setCustomerId(event.target.value)} />
        <Input label="Başlık" value={title} onChange={(event) => setTitle(event.target.value)} />
        <Input
          label="Atanacak Temsilci UUID"
          value={assignedToId}
          onChange={(event) => setAssignedToId(event.target.value)}
        />
      </div>
      {error ? <p className="text-sm text-red-600">{error}</p> : null}
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Oluşturuluyor..." : "Konuşma Oluştur"}
      </Button>
    </form>
  );
}
