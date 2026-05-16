import { FormEvent, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import type { TagCreateRequest, TagResponse, TagUpdateRequest } from "../../types/tag";

type TagFormProps = {
  initialTag?: TagResponse;
  isSubmitting?: boolean;
  submitLabel?: string;
  onSubmit: (request: TagCreateRequest | TagUpdateRequest) => void;
  onCancel?: () => void;
};

export function TagForm({ initialTag, isSubmitting = false, submitLabel = "Kaydet", onSubmit, onCancel }: TagFormProps) {
  const [name, setName] = useState(initialTag?.name ?? "");
  const [color, setColor] = useState(initialTag?.color ?? "#059669");
  const [error, setError] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    if (!name.trim()) {
      setError("Etiket adı zorunludur.");
      return;
    }

    onSubmit({ name: name.trim(), color });
    if (!initialTag) {
      setName("");
      setColor("#059669");
    }
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <div className="grid gap-4 sm:grid-cols-[1fr_140px]">
        <Input label="Etiket Adı" value={name} onChange={(event) => setName(event.target.value)} />
        <Input label="Renk" type="color" value={color} onChange={(event) => setColor(event.target.value)} />
      </div>
      {error ? <p className="text-sm text-red-600">{error}</p> : null}
      <div className="flex flex-wrap gap-2">
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Kaydediliyor..." : submitLabel}
        </Button>
        {onCancel ? (
          <Button type="button" variant="secondary" onClick={onCancel}>
            Vazgeç
          </Button>
        ) : null}
      </div>
    </form>
  );
}
