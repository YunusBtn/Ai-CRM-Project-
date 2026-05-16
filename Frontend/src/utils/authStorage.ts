const TOKEN_KEY = "crm_token";
const EMAIL_KEY = "crm_email";

export const authStorage = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  getEmail: () => localStorage.getItem(EMAIL_KEY),
  setEmail: (email: string) => localStorage.setItem(EMAIL_KEY, email),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
  },
};
