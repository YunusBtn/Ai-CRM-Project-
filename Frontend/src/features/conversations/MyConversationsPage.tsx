import { conversationApi } from "../../api/conversationApi";
import { ConversationListView } from "./ConversationsPage";

export function MyConversationsPage() {
  return (
    <ConversationListView
      title="Benim Konuşmalarım"
      queryKey={["conversations", "my"]}
      queryFn={(page) => conversationApi.getMy({ page, size: 10, sort: "createdAt,desc" })}
    />
  );
}
