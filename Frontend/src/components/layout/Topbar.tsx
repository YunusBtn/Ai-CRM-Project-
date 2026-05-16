import { useNavigate } from "react-router-dom";
import { Button } from "../ui/Button";
import { useAuth } from "../../features/auth/AuthContext";

export function Topbar() {
  const navigate = useNavigate();
  const { email, logout } = useAuth();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <header className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white px-5 py-4">
      <div>
        <p className="text-sm text-slate-500">Hoş geldiniz</p>
        <p className="font-medium text-slate-900">{email || "Kullanıcı"}</p>
      </div>
      <Button variant="secondary" onClick={handleLogout}>
        Çıkış
      </Button>
    </header>
  );
}
