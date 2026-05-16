type EmptyStateProps = {
  title?: string;
  description?: string;
};

export function EmptyState({
  title = "Kayıt bulunamadı",
  description = "Arama veya filtreleri değiştirerek tekrar deneyebilirsiniz.",
}: EmptyStateProps) {
  return (
    <div className="rounded-lg border border-dashed border-slate-300 bg-white p-6 text-center">
      <p className="font-medium text-slate-800">{title}</p>
      <p className="mt-1 text-sm text-slate-500">{description}</p>
    </div>
  );
}
