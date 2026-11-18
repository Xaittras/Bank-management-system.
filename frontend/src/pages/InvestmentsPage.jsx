import { useMemo, useState } from "react";
import AccountPicker from "../components/AccountPicker";
import NeedAccount from "../components/NeedAccount";
import { Sparkline } from "../components/Charts";
import { ASSETS, quote } from "../lib/market";
import { round2 } from "../lib/loans";
import { useStore } from "../lib/store";
import { money } from "../lib/stats";

const EMPTY = [];
const pct = (n) => `${n >= 0 ? "+" : ""}${n.toFixed(2)}%`;

export default function InvestmentsPage({ ctx }) {
  const { auth, selected, transact, notify } = ctx;
  const [holdings, setHoldings] = useStore(auth.userId, "holdings", EMPTY);
  const [symbol, setSymbol] = useState(ASSETS[0].symbol);
  const [side, setSide] = useState("buy");
  const [qty, setQty] = useState("");

  const quotes = useMemo(() => Object.fromEntries(ASSETS.map((a) => [a.symbol, quote(a)])), []);
  const rows = holdings.map((h) => {
    const price = quotes[h.symbol].price;
    return { ...h, price, value: h.qty * price, pl: (price - h.avg) * h.qty };
  });
  const value = rows.reduce((s, r) => s + r.value, 0);
  const cost = rows.reduce((s, r) => s + r.qty * r.avg, 0);

  if (!selected) return <NeedAccount what="start investing" />;

  const asset = ASSETS.find((a) => a.symbol === symbol);
  const price = quotes[symbol].price;
  const q = Number(qty.replace(",", "."));
  const validQty = /^\d+([.,]\d{1,4})?$/.test(qty.trim()) && q > 0;
  const total = validQty ? round2(q * price) : 0;
  const held = holdings.find((h) => h.symbol === symbol);

  const submit = async () => {
    if (!validQty || total <= 0) return notify("Введіть коректну кількість (до 4 знаків після коми)", "error");
    if (side === "sell" && (!held || q > held.qty + 1e-9)) return notify("У вас немає стільки активів", "error");
    if (side === "buy" && total > Number(selected.balance)) return notify("Недостатньо коштів на рахунку", "error");

    // Реальна грошова операція: купівля = зняття, продаж = поповнення
    const ok = await transact(side === "buy" ? "withdraw" : "deposit", selected.id, total, `inv-${symbol}`);
    if (!ok) return;

    setHoldings((prev) => {
      const cur = prev.find((h) => h.symbol === symbol);
      if (side === "buy") {
        if (!cur) return [...prev, { symbol, qty: q, avg: price }];
        const newQty = cur.qty + q;
        return prev.map((h) => h.symbol === symbol ? { ...h, qty: newQty, avg: (cur.qty * cur.avg + q * price) / newQty } : h);
      }
      return prev
        .map((h) => h.symbol === symbol ? { ...h, qty: Math.round((h.qty - q) * 1e4) / 1e4 } : h)
        .filter((h) => h.qty > 1e-9);
    });
    notify(`${side === "buy" ? "Куплено" : "Продано"} ${q} ${symbol} за ${money(total)}`);
    setQty("");
  };

  return (
    <main>
      <section className="full">
        <div className="toolbar">
          <p className="hint">Prices are simulated for demo purposes. Buying and selling move real money on the selected account.</p>
          <AccountPicker ctx={ctx} />
        </div>
      </section>

      <section className="full stat-row">
        <div className="panel stat"><div><small>Portfolio value</small><b>{money(value)}</b></div></div>
        <div className="panel stat"><div><small>Invested</small><b>{money(cost)}</b></div></div>
        <div className="panel stat"><div><small>Profit / loss</small>
          <b className={value - cost >= 0 ? "up" : "down"}>{money(value - cost)}{cost > 0 && ` (${pct(((value - cost) / cost) * 100)})`}</b></div></div>
      </section>

      <section className="cards">
        <h2>Market</h2>
        <div className="panel table-wrap">
          <table>
            <thead><tr><th>Asset</th><th className="num">Price</th><th className="num">24h</th><th>30 days</th></tr></thead>
            <tbody>
              {ASSETS.map((a) => {
                const qt = quotes[a.symbol];
                return (
                  <tr key={a.symbol} className={`pick ${a.symbol === symbol ? "on" : ""}`} onClick={() => setSymbol(a.symbol)}>
                    <td><b>{a.symbol}</b> <span className="muted">{a.name}</span></td>
                    <td className="num">{money(qt.price)}</td>
                    <td className={`num ${qt.change >= 0 ? "up" : "down"}`}>{pct(qt.change)}</td>
                    <td><Sparkline values={qt.series} up={qt.change >= 0} /></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

      <section className="recent">
        <h2>Trade {asset.symbol}</h2>
        <div className="panel form">
          <div className="tabs">
            <button className={side === "buy" ? "on" : ""} onClick={() => setSide("buy")}>Buy</button>
            <button className={side === "sell" ? "on" : ""} onClick={() => setSide("sell")}>Sell</button>
          </div>
          <label htmlFor="qty">Quantity</label>
          <div className="amount"><input id="qty" inputMode="decimal" placeholder="0.5" value={qty} onChange={(e) => setQty(e.target.value)} /></div>
          <div className="kv"><div><span>Price</span><b>{money(price)}</b></div>
            <div><span>{side === "buy" ? "Total cost" : "You receive"}</span><b>{money(total)}</b></div>
            <div><span>You hold</span><b>{held ? held.qty : 0} {symbol}</b></div></div>
          <button className="btn primary" disabled={ctx.busy} onClick={submit}>{side === "buy" ? "Buy" : "Sell"} {symbol}</button>
        </div>
      </section>

      <section className="full">
        <h2>My Holdings</h2>
        <div className="panel table-wrap">
          {rows.length ? (
            <table>
              <thead><tr><th>Asset</th><th className="num">Quantity</th><th className="num">Avg. price</th><th className="num">Value</th><th className="num">P/L</th></tr></thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.symbol}>
                    <td><b>{r.symbol}</b></td><td className="num">{r.qty}</td><td className="num">{money(r.avg)}</td>
                    <td className="num">{money(r.value)}</td>
                    <td className={`num ${r.pl >= 0 ? "up" : "down"}`}>{money(r.pl)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <p className="empty">No investments yet. Pick an asset in the market and buy your first one.</p>}
        </div>
      </section>
    </main>
  );
}
