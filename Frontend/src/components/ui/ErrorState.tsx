import { getApiErrorMessage } from "../../api/axiosClient";

type ErrorStateProps = {
  error: unknown;
};

export function ErrorState({ error }: ErrorStateProps) {
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
      {getApiErrorMessage(error)}
    </div>
  );
}
