import { money } from "../lib/stats";

export default function AccountPicker({ ctx }) {
  const { accounts, selectedAccountId, setSelectedAccountId } = ctx;
  return (
    <select className="select" value={selectedAccountId ?? ""} aria-label="Account"
      onChange={(e) => setSelectedAccountId(Number(e.target.value))}>
      {accounts.map((a) => <option key={a.id} value={a.id}>Account #{a.id} · {money(a.balance)}</option>)}
    </select>
  );
}
