import { useMemo, useState } from "react";
import Icon from "../components/Icons";
import { money, fmtDateTime, totals } from "../lib/stats";

const FILTERS = [["ALL", "All"], ["DEPOSIT", "Deposits"], ["WITHDRAW", "Withdrawals"]];

export default function TransactionsPage({ ctx }) {
  const { accounts, selectedAccountId, setSelectedAccountId, transactions } = ctx;
  const [filter, setFilter] = useState("ALL");

  const sums = useMemo(() => totals(transactions), [transactions]);
  const rows = useMemo(
    () => transactions.filter((t) => filter === "ALL" || String(t.type).toUpperCase() === filter),
    [transactions, filter]
  );

  return (
    <main>
      <section className="full">
        <div className="toolbar">
          <div className="tabs" role="tablist">
            {FILTERS.map(([key, label]) => (
              <button key={key} role="tab" aria-selected={filter === key}
                className={filter === key ? "on" : ""} onClick={() => setFilter(key)}>{label}</button>
            ))}
          </div>
          {accounts.length > 0 && (
            <select className="select" value={selectedAccountId ?? ""} aria-label="Account"
              onChange={(e) => setSelectedAccountId(Number(e.target.value))}>
              {accounts.map((a) => <option key={a.id} value={a.id}>Account #{a.id} · {money(a.balance)}</option>)}
            </select>
          )}
        </div>
      </section>

      <section className="full stat-row">
        <div className="panel stat"><span className="ic dep"><Icon name="plus" /></span>
          <div><small>Total deposits</small><b className="up">{money(sums.deposit)}</b></div></div>
        <div className="panel stat"><span className="ic wd"><Icon name="minus" /></span>
          <div><small>Total withdrawals</small><b className="down">{money(sums.withdraw)}</b></div></div>
      </section>

      <section className="full">
        <div className="panel table-wrap">
          {rows.length ? (
            <table>
              <thead><tr><th>Description</th><th>Date</th><th>Type</th><th className="num">Amount</th></tr></thead>
              <tbody>
                {rows.map((t) => {
                  const dep = String(t.type).toUpperCase() === "DEPOSIT";
                  return (
                    <tr key={t.id}>
                      <td>{t.description || (dep ? "Deposit" : "Withdraw")}</td>
                      <td>{fmtDateTime(t.createdAt)}</td>
                      <td><span className={`badge ${dep ? "" : "admin"}`}>{dep ? "Deposit" : "Withdraw"}</span></td>
                      <td className={`num ${dep ? "up" : "down"}`}>{dep ? "+" : "-"}{money(t.amount)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          ) : (
            <p className="empty">{accounts.length ? "No transactions found" : "Create an account first"}</p>
          )}
        </div>
      </section>
    </main>
  );
}
