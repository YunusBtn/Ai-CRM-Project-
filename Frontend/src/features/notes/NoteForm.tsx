import { FormEvent, useState } from "react";
import { Button } from "../../components/ui/Button";

type NoteFormProps = {
  initialContent?: string;
  submitLabel?: string;
  isSubmitting?: boolean;
  onSubmit: (content: string) => void;
  onCancel?: () => void;
};

export function NoteForm({
  initialContent = "",
  submitLabel = "Kaydet",
  isSubmitting = false,
  onSubmit,
  onCancel,
}: NoteFormProps) {
  const [content, setContent] = useState(initialContent);
  const [error, setError] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    if (!content.trim()) {
      setError("Not içeriği zorunludur.");
      return;
    }

    onSubmit(content.trim());
    if (!initialContent) {
      setContent("");
    }
  }

  return (
    <form className="space-y-3" onSubmit={handleSubmit}>
      <textarea
        className="min-h-24 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-100"
        value={content}
        onChange={(event) => setContent(event.target.value)}
        placeholder="Not yazın..."
      />
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
