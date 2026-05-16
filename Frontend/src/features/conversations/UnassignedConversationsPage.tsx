import { conversationApi } from "../../api/conversationApi";
import { ConversationListView } from "./ConversationsPage";

export function UnassignedConversationsPage() {
  return (
    <ConversationListView
      title="Atanmamış Konuşmalar"
      queryKey={["conversations", "unassigned"]}
      queryFn={(page) => conversationApi.getUnassigned({ page, size: 10, sort: "createdAt,desc" })}
    />
  );
}
