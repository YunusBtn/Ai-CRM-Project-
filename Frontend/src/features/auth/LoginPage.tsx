import { FormEvent, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { authApi } from "../../api/authApi";
import { getApiErrorMessage } from "../../api/axiosClient";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { useAuth } from "./AuthContext";

export function LoginPage() {
  const navigate = useNavigate();
  const { isAuthenticated, setSession } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [formError, setFormError] = useState("");

  const loginMutation = useMutation({
    mutationFn: authApi.login,
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

    if (!email.trim() || !password.trim()) {
      setFormError("Email ve şifre zorunludur.");
      return;
    }

    loginMutation.mutate({ email, password });
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
      <section className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8">
          <p className="text-sm font-semibold text-brand-700">AI CRM</p>
          <h1 className="mt-2 text-2xl font-bold text-slate-950">Giriş Yap</h1>
          <p className="mt-2 text-sm text-slate-500">CRM panelinize erişmek için bilgilerinizi girin.</p>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <Input label="Email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} />
          <Input
            label="Şifre"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          {formError ? <p className="text-sm text-red-600">{formError}</p> : null}
          <Button className="w-full" type="submit" disabled={loginMutation.isPending}>
            {loginMutation.isPending ? "Giriş yapılıyor..." : "Giriş Yap"}
          </Button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-600">
          Hesabınız yok mu?{" "}
          <Link className="font-medium text-brand-700 hover:underline" to="/register">
            Kayıt Ol
          </Link>
        </p>
      </section>
    </main>
  );
}
