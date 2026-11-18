import { useMemo } from "react";
import { Link } from "react-router-dom";
import BankCard from "../components/BankCard";
import NewAccountForm from "../components/NewAccountForm";
import Icon from "../components/Icons";
import { WeeklyChart, SplitChart, BalanceChart } from "../components/Charts";
import { COLORS, money, fmtDate, weeklyActivity, totals, balanceHistory } from "../lib/stats";

export default function Dashboard({ ctx }) {
  const { accounts, holder, selectedAccountId, setSelectedAccountId, transactions, selected,
    amount, setAmount, busy, runBalanceOperation } = ctx;

  // Дані для графіків рахуються з реальних транзакцій вибраного рахунку
  const week = useMemo(() => weeklyActivity(transactions), [transactions]);
  const split = useMemo(() => totals(transactions), [transactions]);
  const history = useMemo(() => balanceHistory(transactions, selected?.balance ?? 0), [transactions, selected]);

  return (
    <main>
      <section className="cards">
        <h2 className="h2-row">My Cards <Link to="/bank/credit-cards">See All</Link></h2>
        <div className="cards-row">
          {accounts.map((acc, i) => (
            <BankCard key={acc.id} account={acc} holder={holder} blue={i % 2 === 0}
              selected={acc.id === selectedAccountId} onSelect={setSelectedAccountId} />
          ))}
          <NewAccountForm ctx={ctx} />
        </div>
      </section>

      <section className="recent">
        <h2 className="h2-row">Recent Transaction <Link to="/bank/transactions">See All</Link></h2>
        <div className="panel">
          {transactions.length ? (
            <ul className="tx">
              {transactions.slice(0, 5).map((t) => {
                const dep = String(t.type).toUpperCase() === "DEPOSIT";
                return (
                  <li key={t.id}>
                    <span className={`ic ${dep ? "dep" : "wd"}`}><Icon name={dep ? "plus" : "minus"} /></span>
                    <div className="t">
                      <b>{t.description || (dep ? "Deposit" : "Withdraw")}</b>
                      <span>{fmtDate(t.createdAt)}</span>
                    </div>
                    <span className={dep ? "up" : "down"}>{dep ? "+" : "-"}{money(t.amount)}</span>
                  </li>
                );
              })}
            </ul>
          ) : (
            <p className="empty">No transactions yet</p>
          )}
        </div>
      </section>

      <section className="weekly">
        <h2>Weekly Activity</h2>
        <div className="panel">
          <div className="legend">
            <span><i style={{ background: COLORS.deposit }} />Deposit</span>
            <span><i style={{ background: COLORS.withdraw }} />Withdraw</span>
          </div>
          <WeeklyChart days={week} />
        </div>
      </section>

      <section className="expense">
        <h2>Deposits vs Withdrawals</h2>
        <div className="panel pie-wrap"><SplitChart deposit={split.deposit} withdraw={split.withdraw} /></div>
      </section>

      <section className="transfer">
        <h2>Quick Operation</h2>
        <div className="panel">
          {selected ? (
            <>
              <div className="selected-acc">
                <span className="ic dep"><Icon name="wallet" /></span>
                <div><b>Account #{selected.id}</b><span>Available: {money(selected.balance)}</span></div>
              </div>
              <div className="send-row">
                <label htmlFor="amt">Write Amount</label>
                <div className="amount">
                  <input id="amt" inputMode="decimal" placeholder="525.50" value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && runBalanceOperation("deposit")} />
                  <button className="send" disabled={busy} onClick={() => runBalanceOperation("deposit")}>
                    Deposit <Icon name="plus" size={20} />
                  </button>
                </div>
              </div>
              <button className="btn ghost" disabled={busy} onClick={() => runBalanceOperation("withdraw")}>
                Withdraw <Icon name="minus" size={18} />
              </button>
            </>
          ) : (
            <p className="empty">Create an account to make deposits and withdrawals</p>
          )}
        </div>
      </section>

      <section className="balance-history">
        <h2>Balance History</h2>
        <div className="panel"><BalanceChart points={history} /></div>
      </section>
    </main>
  );
}
