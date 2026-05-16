import { FormEvent, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { authApi } from "../../api/authApi";
import { getApiErrorMessage } from "../../api/axiosClient";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { useAuth } from "./AuthContext";

export function RegisterPage() {
  const navigate = useNavigate();
  const { isAuthenticated, setSession } = useAuth();
  const [form, setForm] = useState({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
  });
  const [formError, setFormError] = useState("");

  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (response) => {
      setSession(response);
      navigate("/dashboard", { replace: true });
    },
    onError: (error) => setFormError(getApiErrorMessage(error)),
  });

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError("");

    if (!form.email.trim() || !form.password.trim() || !form.firstName.trim() || !form.lastName.trim()) {
      setFormError("Tüm alanlar zorunludur.");
      return;
    }

    registerMutation.mutate(form);
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
      <section className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8">
          <p className="text-sm font-semibold text-brand-700">AI CRM</p>
          <h1 className="mt-2 text-2xl font-bold text-slate-950">Kayıt Ol</h1>
          <p className="mt-2 text-sm text-slate-500">Yeni CRM kullanıcısı oluşturun.</p>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Ad"
              value={form.firstName}
              onChange={(event) => setForm((current) => ({ ...current, firstName: event.target.value }))}
            />
            <Input
              label="Soyad"
              value={form.lastName}
              onChange={(event) => setForm((current) => ({ ...current, lastName: event.target.value }))}
            />
          </div>
          <Input
            label="Email"
            type="email"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
          />
          <Input
            label="Şifre"
            type="password"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
          />
          {formError ? <p className="text-sm text-red-600">{formError}</p> : null}
          <Button className="w-full" type="submit" disabled={registerMutation.isPending}>
            {registerMutation.isPending ? "Kayıt oluşturuluyor..." : "Kayıt Ol"}
          </Button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-600">
          Zaten hesabınız var mı?{" "}
          <Link className="font-medium text-brand-700 hover:underline" to="/login">
            Giriş Yap
          </Link>
        </p>
      </section>
    </main>
  );
}
