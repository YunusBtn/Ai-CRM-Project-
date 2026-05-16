import { FormEvent, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import type { CustomerCreateRequest, CustomerResponse, CustomerStatus, CustomerUpdateRequest } from "../../types/customer";

type CustomerFormProps = {
  initialCustomer?: CustomerResponse;
  isSubmitting?: boolean;
  onSubmit: (request: CustomerCreateRequest | CustomerUpdateRequest) => void;
};

const statusOptions = [
  { label: "Aktif", value: "ACTIVE" },
  { label: "Pasif", value: "PASSIVE" },
  { label: "Engelli", value: "BLOCKED" },
];

export function CustomerForm({ initialCustomer, isSubmitting = false, onSubmit }: CustomerFormProps) {
  const [form, setForm] = useState({
    firstName: initialCustomer?.firstName ?? "",
    lastName: initialCustomer?.lastName ?? "",
    phone: initialCustomer?.phone ?? "",
    email: initialCustomer?.email ?? "",
    status: initialCustomer?.status ?? "ACTIVE",
  });
  const [error, setError] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    if (!form.phone.trim() && !form.email.trim()) {
      setError("Telefon veya email alanlarından en az biri zorunludur.");
      return;
    }

    const request = {
      firstName: form.firstName.trim() || undefined,
      lastName: form.lastName.trim() || undefined,
      phone: form.phone.trim() || undefined,
      email: form.email.trim() || undefined,
      ...(initialCustomer ? { status: form.status as CustomerStatus } : {}),
    };

    onSubmit(request);
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Ad" value={form.firstName} onChange={(event) => setForm((current) => ({ ...current, firstName: event.target.value }))} />
        <Input label="Soyad" value={form.lastName} onChange={(event) => setForm((current) => ({ ...current, lastName: event.target.value }))} />
        <Input label="Telefon" value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} />
        <Input label="Email" type="email" value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} />
      </div>
      {initialCustomer ? (
        <Select
          label="Durum"
          options={statusOptions}
          value={form.status}
          onChange={(event) => setForm((current) => ({ ...current, status: event.target.value as CustomerStatus }))}
        />
      ) : null}
      {error ? <p className="text-sm text-red-600">{error}</p> : null}
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Kaydediliyor..." : initialCustomer ? "Güncelle" : "Müşteri Oluştur"}
      </Button>
    </form>
  );
}
