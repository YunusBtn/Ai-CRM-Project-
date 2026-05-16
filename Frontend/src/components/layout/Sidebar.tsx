import { NavLink } from "react-router-dom";

const menuItems = [
  { label: "Panel", to: "/dashboard" },
  { label: "Müşteriler", to: "/customers" },
  { label: "Konuşmalar", to: "/conversations" },
  { label: "Benim Konuşmalarım", to: "/conversations/my" },
  { label: "Atanmamış Konuşmalar", to: "/conversations/unassigned" },
  { label: "Cevap Bekleyenler", to: "/conversations/waiting-reply" },
  { label: "Etiketler", to: "/tags" },
];

export function Sidebar() {
  return (
    <aside className="w-full border-b border-slate-200 bg-slate-950 text-white md:min-h-screen md:w-72 md:border-b-0">
      <div className="px-5 py-5">
        <p className="text-lg font-bold">AI CRM</p>
        <p className="mt-1 text-xs text-slate-400">Yönetim Paneli</p>
      </div>
      <nav className="flex gap-1 overflow-x-auto px-3 pb-3 md:block md:space-y-1 md:overflow-visible">
        {menuItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `block whitespace-nowrap rounded-md px-3 py-2 text-sm transition ${
                isActive ? "bg-white text-slate-950" : "text-slate-300 hover:bg-slate-800 hover:text-white"
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
