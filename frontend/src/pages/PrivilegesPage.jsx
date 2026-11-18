import { Link } from "react-router-dom";
import Icon from "../components/Icons";
import { TIERS } from "../lib/tiers";
import { money } from "../lib/stats";

export default function PrivilegesPage({ ctx }) {
  const { tier, index, next, progress, total } = ctx.tierInfo;

  return (
    <main>
      <section className="full">
        <div className="panel tier-hero">
          <span className="ic wd"><Icon name="star" size={28} /></span>
          <div>
            <small className="muted">Your tier</small>
            <h2>{tier.name}</h2>
            <p className="muted">Total balance across {ctx.accounts.length} account{ctx.accounts.length === 1 ? "" : "s"}: <b>{money(total)}</b></p>
          </div>
          <div className="tier-progress">
            {next ? (
              <>
                <span>{money(next.min - total)} to {next.name}</span>
                <div className="progress"><i style={{ width: `${Math.min(progress, 1) * 100}%` }} /></div>
              </>
            ) : <span>You have reached the highest tier</span>}
          </div>
        </div>
      </section>

      <section className="full">
        <h2>Benefits</h2>
        <div className="tier-grid">
          {TIERS.map((t, i) => (
            <div key={t.name} className={`panel tier ${i === index ? "current" : ""} ${i > index ? "locked" : ""}`}>
              <div className="loan-head"><b>{t.name}</b>
                <span className="badge">{i === index ? "Current" : i < index ? "Unlocked" : "Locked"}</span></div>
              <small className="muted">from {money(t.min)}</small>
              <ul>{t.perks.map((p) => <li key={p}>{p}</li>)}</ul>
            </div>
          ))}
        </div>
      </section>

      <section className="full">
        {tier.rateDiscount > 0
          ? <Link className="btn primary narrow" to="/bank/loans">Use your −{tier.rateDiscount} pp loan discount</Link>
          : <Link className="btn ghost narrow" to="/bank">Top up to reach Silver</Link>}
      </section>
    </main>
  );
}
