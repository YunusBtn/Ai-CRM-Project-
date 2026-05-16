import { conversationApi } from "../../api/conversationApi";
import { ConversationListView } from "./ConversationsPage";

export function WaitingReplyConversationsPage() {
  return (
    <ConversationListView
      title="Cevap Bekleyen Konuşmalar"
      description="Son mesajı müşteri tarafından gelen konuşmalar."
      queryKey={["conversations", "waiting-reply"]}
      queryFn={(page) => conversationApi.getWaitingReply({ page, size: 10, sort: "createdAt,desc" })}
    />
  );
}
