import type { SelectHTMLAttributes } from "react";

type Option = {
  label: string;
  value: string;
};

type SelectProps = SelectHTMLAttributes<HTMLSelectElement> & {
  label?: string;
  error?: string;
  options: Option[];
};

export function Select({ label, error, options, className = "", id, ...props }: SelectProps) {
  const selectId = id || props.name;

  return (
    <label className="block text-sm">
      {label ? <span className="mb-1 block font-medium text-slate-700">{label}</span> : null}
      <select
        id={selectId}
        className={`h-10 w-full rounded-md border border-slate-300 bg-white px-3 text-slate-900 outline-none transition focus:border-brand-600 focus:ring-2 focus:ring-brand-100 ${className}`}
        {...props}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error ? <span className="mt-1 block text-xs text-red-600">{error}</span> : null}
    </label>
  );
}
