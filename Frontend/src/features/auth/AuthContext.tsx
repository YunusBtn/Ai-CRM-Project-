import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import type { AuthResponse } from "../../types/auth";
import { authStorage } from "../../utils/authStorage";

type AuthContextValue = {
  token: string | null;
  email: string | null;
  isAuthenticated: boolean;
  setSession: (response: AuthResponse) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState(() => authStorage.getToken());
  const [email, setEmail] = useState(() => authStorage.getEmail());

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      email,
      isAuthenticated: Boolean(token),
      setSession: (response) => {
        authStorage.setToken(response.token);
        authStorage.setEmail(response.email);
        setToken(response.token);
        setEmail(response.email);
      },
      logout: () => {
        authStorage.clear();
        setToken(null);
        setEmail(null);
      },
    }),
    [email, token],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth AuthProvider içinde kullanılmalı.");
  }

  return context;
}
