import { useQuery } from "@tanstack/react-query";
import { dashboardApi } from "../../api/dashboardApi";
import { Card } from "../../components/ui/Card";
import { EmptyState } from "../../components/ui/EmptyState";
import { ErrorState } from "../../components/ui/ErrorState";
import { Loading } from "../../components/ui/Loading";

const summaryCards = [
  { key: "totalCustomerCount", label: "Toplam Müşteri" },
  { key: "activeCustomerCount", label: "Aktif Müşteri" },
  { key: "todayCreatedCustomerCount", label: "Bugün Oluşturulan Müşteri" },
  { key: "openConversationCount", label: "Açık Konuşma" },
  { key: "pendingConversationCount", label: "Pending Konuşma" },
  { key: "waitingReplyConversationCount", label: "Cevap Bekleyen" },
  { key: "todayInboundMessageCount", label: "Bugün Gelen Mesaj" },
  { key: "unassignedConversationCount", label: "Atanmamış Konuşma" },
  { key: "todayClosedConversationCount", label: "Bugün Kapanan Konuşma" },
  { key: "myAssignedOpenConversationCount", label: "Bana Atanmış Açık Konuşma" },
] as const;

export function DashboardPage() {
  const summaryQuery = useQuery({
    queryKey: ["dashboard", "summary"],
    queryFn: dashboardApi.getSummary,
  });

  const statusQuery = useQuery({
    queryKey: ["dashboard", "conversation-status-distribution"],
    queryFn: dashboardApi.getConversationStatusDistribution,
  });

  const tagQuery = useQuery({
    queryKey: ["dashboard", "customer-tag-distribution"],
    queryFn: dashboardApi.getCustomerTagDistribution,
  });

  if (summaryQuery.isLoading) {
    return <Loading />;
  }

  if (summaryQuery.isError) {
    return <ErrorState error={summaryQuery.error} />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-950">Panel</h1>
        <p className="mt-1 text-sm text-slate-500">CRM genel durum özeti</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        {summaryCards.map((card) => (
          <Card key={card.key}>
            <p className="text-sm text-slate-500">{card.label}</p>
            <p className="mt-2 text-2xl font-bold text-slate-950">{summaryQuery.data?.[card.key] ?? 0}</p>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card title="Konuşma Durum Dağılımı">
          {statusQuery.isLoading ? <Loading /> : null}
          {statusQuery.isError ? <ErrorState error={statusQuery.error} /> : null}
          {statusQuery.data && statusQuery.data.length > 0 ? (
            <div className="space-y-3">
              {statusQuery.data.map((item) => (
                <div key={item.status} className="flex items-center justify-between rounded-md bg-slate-50 px-3 py-2">
                  <span className="font-medium text-slate-700">{item.status}</span>
                  <span className="text-slate-900">{item.count}</span>
                </div>
              ))}
            </div>
          ) : null}
          {statusQuery.data?.length === 0 ? <EmptyState title="Durum verisi yok" description="Henüz konuşma dağılımı oluşmamış." /> : null}
        </Card>

        <Card title="Etiket Müşteri Dağılımı">
          {tagQuery.isLoading ? <Loading /> : null}
          {tagQuery.isError ? <ErrorState error={tagQuery.error} /> : null}
          {tagQuery.data && tagQuery.data.length > 0 ? (
            <div className="space-y-3">
              {tagQuery.data.map((item) => (
                <div key={item.tagId} className="flex items-center justify-between rounded-md bg-slate-50 px-3 py-2">
                  <span className="font-medium text-slate-700">{item.tagName}</span>
                  <span className="text-slate-900">{item.customerCount}</span>
                </div>
              ))}
            </div>
          ) : null}
          {tagQuery.data?.length === 0 ? <EmptyState title="Etiket verisi yok" description="Henüz etiketli müşteri bulunmuyor." /> : null}
        </Card>
      </div>
    </div>
  );
}
