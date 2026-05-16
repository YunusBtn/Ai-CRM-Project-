import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppLayout } from "../components/layout/AppLayout";
import { LoginPage } from "../features/auth/LoginPage";
import { RegisterPage } from "../features/auth/RegisterPage";
import { ProtectedRoute } from "../features/auth/ProtectedRoute";
import { DashboardPage } from "../features/dashboard/DashboardPage";
import { CustomersPage } from "../features/customers/CustomersPage";
import { CustomerDetailPage } from "../features/customers/CustomerDetailPage";
import { ConversationsPage } from "../features/conversations/ConversationsPage";
import { ConversationDetailPage } from "../features/conversations/ConversationDetailPage";
import { MyConversationsPage } from "../features/conversations/MyConversationsPage";
import { UnassignedConversationsPage } from "../features/conversations/UnassignedConversationsPage";
import { WaitingReplyConversationsPage } from "../features/conversations/WaitingReplyConversationsPage";
import { TagsPage } from "../features/tags/TagsPage";
import { authStorage } from "../utils/authStorage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: authStorage.getToken() ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />,
  },
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: "/dashboard", element: <DashboardPage /> },
          { path: "/customers", element: <CustomersPage /> },
          { path: "/customers/:id", element: <CustomerDetailPage /> },
          { path: "/conversations", element: <ConversationsPage /> },
          { path: "/conversations/my", element: <MyConversationsPage /> },
          { path: "/conversations/unassigned", element: <UnassignedConversationsPage /> },
          { path: "/conversations/waiting-reply", element: <WaitingReplyConversationsPage /> },
          { path: "/conversations/:id", element: <ConversationDetailPage /> },
          { path: "/tags", element: <TagsPage /> },
        ],
      },
    ],
  },
  { path: "*", element: <Navigate to="/" replace /> },
]);
