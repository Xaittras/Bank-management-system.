import { useState } from "react";
import AccountPicker from "../components/AccountPicker";
import NeedAccount from "../components/NeedAccount";
import Icon from "../components/Icons";
import { useStore } from "../lib/store";
import { money, parseAmount } from "../lib/stats";

const SERVICES = [
  { id: "electricity", name: "Electricity", provider: "City Energy", icon: "bolt", hint: "Contract number" },
  { id: "water", name: "Water", provider: "Aqua Service", icon: "drop", hint: "Account number" },
  { id: "internet", name: "Internet", provider: "FiberNet", icon: "wifi", hint: "Login or contract" },
  { id: "mobile", name: "Mobile", provider: "Mobile Plus", icon: "phone", hint: "Phone number" },
  { id: "streaming", name: "Streaming", provider: "StreamBox", icon: "tv", hint: "Email" },
  { id: "insurance", name: "Insurance", provider: "SafeLife", icon: "shield", hint: "Policy number" },
];
const EMPTY = [];

export default function ServicesPage({ ctx }) {
  const { auth, selected, transact, busy, notify } = ctx;
  const [payments, setPayments] = useStore(auth.userId, "payments", EMPTY);
  const [serviceId, setServiceId] = useState(SERVICES[0].id);
  const [ref, setRef] = useState("");
  const [amount, setAmount] = useState("");

  if (!selected) return <NeedAccount what="pay for services" />;

  const service = SERVICES.find((s) => s.id === serviceId);
  const now = new Date();
  const monthTotal = payments
    .filter((p) => { const d = new Date(p.date); return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear(); })
    .reduce((s, p) => s + p.amount, 0);

  const pay = async () => {
    const value = parseAmount(amount);
    if (ref.trim().length < 3) return notify(`Вкажіть: ${service.hint.toLowerCase()}`, "error");
    if (!value) return notify("Введіть додатну суму (максимум 2 знаки після коми)", "error");
    if (value > Number(selected.balance)) return notify("Недостатньо коштів на рахунку", "error");

    const ok = await transact("withdraw", selected.id, value, `svc-${serviceId}-${ref.trim()}`);
    if (!ok) return;
    setPayments((prev) => [{ id: Date.now(), serviceId, ref: ref.trim(), amount: value, date: new Date().toISOString(), accountId: selected.id }, ...prev]);
    notify(`${service.name}: сплачено ${money(value)}`);
    setAmount("");
  };

  return (
    <main>
      <section className="full">
        <div className="toolbar">
          <p className="hint">Payments are withdrawn from the selected account.</p>
          <AccountPicker ctx={ctx} />
        </div>
      </section>

      <section className="cards">
        <h2>Pay for a service</h2>
        <div className="panel">
          <div className="tiles">
            {SERVICES.map((s) => (
              <button key={s.id} className={`tile ${s.id === serviceId ? "on" : ""}`} onClick={() => setServiceId(s.id)} aria-pressed={s.id === serviceId}>
                <span className="ic wd"><Icon name={s.icon} /></span>
                <b>{s.name}</b><small>{s.provider}</small>
              </button>
            ))}
          </div>
          <div className="send-row">
            <label htmlFor="ref">{service.hint}</label>
            <div className="amount"><input id="ref" value={ref} maxLength={30} onChange={(e) => setRef(e.target.value)} /></div>
          </div>
          <div className="send-row">
            <label htmlFor="svc-amt">Amount</label>
            <div className="amount">
              <input id="svc-amt" inputMode="decimal" placeholder="45.00" value={amount} onChange={(e) => setAmount(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && pay()} />
              <button className="send" disabled={busy} onClick={pay}>Pay</button>
            </div>
          </div>
        </div>
      </section>

      <section className="recent">
        <h2>This month</h2>
        <div className="panel stat"><div><small>Spent on services</small><b>{money(monthTotal)}</b></div></div>
      </section>

      <section className="full">
        <h2>Payment history</h2>
        <div className="panel table-wrap">
          {payments.length ? (
            <table>
              <thead><tr><th>Service</th><th>Reference</th><th>Date</th><th>Account</th><th className="num">Amount</th></tr></thead>
              <tbody>
                {payments.map((p) => (
                  <tr key={p.id}>
                    <td>{SERVICES.find((s) => s.id === p.serviceId)?.name}</td><td>{p.ref}</td>
                    <td>{new Date(p.date).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" })}</td>
                    <td>#{p.accountId}</td><td className="num down">-{money(p.amount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <p className="empty">No payments yet</p>}
        </div>
      </section>
    </main>
  );
}
