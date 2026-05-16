import type { ReactNode } from "react";

type BadgeTone = "green" | "yellow" | "red" | "slate" | "blue";

type BadgeProps = {
  children: ReactNode;
  tone?: BadgeTone;
  color?: string;
};

const tones: Record<BadgeTone, string> = {
  green: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  yellow: "bg-amber-50 text-amber-700 ring-amber-200",
  red: "bg-red-50 text-red-700 ring-red-200",
  slate: "bg-slate-100 text-slate-700 ring-slate-200",
  blue: "bg-sky-50 text-sky-700 ring-sky-200",
};

export function Badge({ children, tone = "slate", color }: BadgeProps) {
  if (color) {
    return (
      <span
        className="inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ring-1 ring-inset"
        style={{
          backgroundColor: `${color}22`,
          color,
          borderColor: color,
        }}
      >
        {children}
      </span>
    );
  }

  return (
    <span className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ring-1 ring-inset ${tones[tone]}`}>
      {children}
    </span>
  );
}
