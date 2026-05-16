import { FormEvent, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Select } from "../../components/ui/Select";
import type { MessageCreateRequest, MessageDirection, SenderType } from "../../types/message";

type MessageFormProps = {
  isSubmitting?: boolean;
  onSubmit: (request: MessageCreateRequest) => void;
};

const directionOptions = [
  { label: "INBOUND", value: "INBOUND" },
  { label: "OUTBOUND", value: "OUTBOUND" },
];

const senderOptions = [
  { label: "CUSTOMER", value: "CUSTOMER" },
  { label: "AGENT", value: "AGENT" },
  { label: "SYSTEM", value: "SYSTEM" },
];

function isValidCombination(direction: MessageDirection, senderType: SenderType) {
  return (
    (direction === "INBOUND" && senderType === "CUSTOMER") ||
    (direction === "OUTBOUND" && senderType === "AGENT") ||
    (direction === "OUTBOUND" && senderType === "SYSTEM")
  );
}

export function MessageForm({ isSubmitting = false, onSubmit }: MessageFormProps) {
  const [content, setContent] = useState("");
  const [messageDirection, setMessageDirection] = useState<MessageDirection>("OUTBOUND");
  const [senderType, setSenderType] = useState<SenderType>("AGENT");
  const [error, setError] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    if (!content.trim()) {
      setError("Mesaj içeriği zorunludur.");
      return;
    }

    if (!isValidCombination(messageDirection, senderType)) {
      setError("Geçerli kombinasyon seçin: INBOUND + CUSTOMER, OUTBOUND + AGENT veya OUTBOUND + SYSTEM.");
      return;
    }

    onSubmit({ content: content.trim(), messageDirection, senderType });
    setContent("");
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <div className="rounded-md bg-slate-50 p-3 text-sm text-slate-600">
        Geçerli kombinasyonlar: INBOUND + CUSTOMER, OUTBOUND + AGENT, OUTBOUND + SYSTEM.
      </div>
      <textarea
        className="min-h-28 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-100"
        value={content}
        onChange={(event) => setContent(event.target.value)}
        placeholder="Mesaj içeriği..."
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <Select
          label="Yön"
          value={messageDirection}
          onChange={(event) => setMessageDirection(event.target.value as MessageDirection)}
          options={directionOptions}
        />
        <Select
          label="Gönderen Tipi"
          value={senderType}
          onChange={(event) => setSenderType(event.target.value as SenderType)}
          options={senderOptions}
        />
      </div>
      {error ? <p className="text-sm text-red-600">{error}</p> : null}
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Gönderiliyor..." : "Mesaj Ekle"}
      </Button>
    </form>
  );
}
