import { useState } from "react";
import { Link } from "react-router-dom";
import AccountPicker from "../components/AccountPicker";
import NeedAccount from "../components/NeedAccount";
import { OFFERS, buildSchedule, monthly, round2 } from "../lib/loans";
import { useStore } from "../lib/store";
import { money, fmtDate } from "../lib/stats";

const EMPTY = [];
const clamp = (v, lo, hi) => Math.min(Math.max(v, lo), hi);

export default function LoansPage({ ctx }) {
  const { auth, selected, transact, busy, notify, tierInfo } = ctx;
  const [loans, setLoans] = useStore(auth.userId, "loans", EMPTY);
  const [offerId, setOfferId] = useState(OFFERS[0].id);
  const [amount, setAmount] = useState(5000);
  const [term, setTerm] = useState(24);

  if (!selected) return <NeedAccount what="take a loan" />;

  const offer = OFFERS.find((o) => o.id === offerId);
  const discount = tierInfo.tier.rateDiscount;
  const rate = Math.max(offer.rate - discount, 1);
  const pay = round2(monthly(amount, rate, term));
  const totalPayable = round2(pay * term);

  const pickOffer = (o) => {
    setOfferId(o.id);
    setAmount(clamp(amount, o.min, o.max));
    setTerm(clamp(term, o.minTerm, o.maxTerm));
  };

  const apply = async () => {
    // Реальна операція: сума кредиту зараховується на рахунок
    const ok = await transact("deposit", selected.id, amount, "loan");
    if (!ok) return;
    setLoans((prev) => [...prev, { id: Date.now(), name: offer.name, principal: amount, rate, term, start: new Date().toISOString(), paid: 0 }]);
    notify(`Кредит ${money(amount)} зараховано на рахунок #${selected.id}`);
  };

  const payInstallment = async (loan, row) => {
    if (Number(selected.balance) < row.payment) return notify("Недостатньо коштів для платежу", "error");
    const ok = await transact("withdraw", selected.id, row.payment, `loan${loan.id}-${row.n}`);
    if (!ok) return;
    setLoans((prev) => prev.map((l) => (l.id === loan.id ? { ...l, paid: l.paid + 1 } : l)));
    notify(`Платіж ${row.n}/${loan.term} сплачено`);
  };

  return (
    <main>
      <section className="full">
        <div className="toolbar">
          <p className="hint">
            Your {tierInfo.tier.name} tier {discount ? `lowers every rate by ${discount} pp.` : "has standard rates."}{" "}
            <Link className="link" to="/bank/privileges">See privileges</Link>
          </p>
          <AccountPicker ctx={ctx} />
        </div>
      </section>

      <section className="transfer">
        <h2>Loan calculator</h2>
        <div className="panel form">
          <div className="tabs">
            {OFFERS.map((o) => (
              <button key={o.id} className={o.id === offerId ? "on" : ""} onClick={() => pickOffer(o)}>{o.name.replace(" loan", "")}</button>
            ))}
          </div>
          <label htmlFor="amount">Amount: <b>{money(amount)}</b></label>
          <input id="amount" type="range" min={offer.min} max={offer.max} step={100} value={amount} onChange={(e) => setAmount(Number(e.target.value))} />
          <label htmlFor="term">Term: <b>{term} months</b></label>
          <input id="term" type="range" min={offer.minTerm} max={offer.maxTerm} step={1} value={term} onChange={(e) => setTerm(Number(e.target.value))} />
          <div className="kv">
            <div><span>Interest rate</span><b>{rate}% {discount > 0 && <small className="muted">(−{discount} pp)</small>}</b></div>
            <div><span>Monthly payment</span><b>{money(pay)}</b></div>
            <div><span>Total interest</span><b>{money(totalPayable - amount)}</b></div>
            <div><span>Total payable</span><b>{money(totalPayable)}</b></div>
          </div>
          <button className="btn primary" disabled={busy} onClick={apply}>Take loan</button>
        </div>
      </section>

      <section className="balance-history">
        <h2>My Loans</h2>
        {loans.length ? [...loans].reverse().map((loan) => {
          const sched = buildSchedule(loan);
          const done = loan.paid >= loan.term;
          const left = loan.paid ? sched[loan.paid - 1].balance : loan.principal;
          const next = sched[loan.paid];
          return (
            <div className="panel loan" key={loan.id}>
              <div className="loan-head">
                <div><b>{loan.name}</b><span className="muted"> · {money(loan.principal)} at {loan.rate}% · {fmtDate(loan.start)}</span></div>
                {done ? <span className="badge">Paid off</span> : <span className="badge">{loan.paid}/{loan.term}</span>}
              </div>
              <div className="progress" role="progressbar" aria-valuenow={loan.paid} aria-valuemax={loan.term}>
                <i style={{ width: `${(loan.paid / loan.term) * 100}%` }} />
              </div>
              <div className="kv">
                <div><span>Remaining balance</span><b>{money(left)}</b></div>
                {next && <div><span>Next payment · {fmtDate(next.date)}</span><b>{money(next.payment)}</b></div>}
              </div>
              {next && <button className="btn ghost" disabled={busy} onClick={() => payInstallment(loan, next)}>Pay installment</button>}
              <details>
                <summary>Payment schedule</summary>
                <table>
                  <thead><tr><th>#</th><th>Date</th><th className="num">Payment</th><th className="num">Interest</th><th className="num">Balance</th></tr></thead>
                  <tbody>
                    {sched.slice(loan.paid, loan.paid + 12).map((r) => (
                      <tr key={r.n}><td>{r.n}</td><td>{fmtDate(r.date)}</td><td className="num">{money(r.payment)}</td><td className="num">{money(r.interest)}</td><td className="num">{money(r.balance)}</td></tr>
                    ))}
                  </tbody>
                </table>
              </details>
            </div>
          );
        }) : <div className="panel"><p className="empty">You have no loans. Use the calculator to take one.</p></div>}
      </section>
    </main>
  );
}
