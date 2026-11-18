import { compact, niceScale, COLORS } from "../lib/stats";

const Empty = ({ children }) => <p className="empty">{children}</p>;

/* ---------- Weekly Activity: стовпчики за 7 днів ---------- */
export function WeeklyChart({ days }) {
  const dataMax = Math.max(0, ...days.flatMap((d) => [d.deposit, d.withdraw]));
  if (!dataMax) return <Empty>No activity in the last 7 days</Empty>;

  const { max, step } = niceScale(dataMax);
  const L = 56, R = 610, T = 14, B = 210;
  const y = (v) => B - (v / max) * (B - T);
  const slot = (R - L) / days.length;
  const ticks = [];
  for (let v = 0; v <= max; v += step) ticks.push(v);

  return (
    <svg className="chart" viewBox="0 0 620 250" role="img" aria-label="Deposits and withdrawals for the last 7 days">
      {ticks.map((v) => (
        <g key={v}>
          <line x1={L} x2={R} y1={y(v)} y2={y(v)} stroke="#f0f1f7" />
          <text x={L - 12} y={y(v) + 4} textAnchor="end">{compact(v)}</text>
        </g>
      ))}
      {days.map((d, i) => {
        const cx = L + slot * i + slot / 2;
        return (
          <g key={d.key}>
            <rect x={cx - 22} y={y(d.withdraw)} width="15" height={Math.max(B - y(d.withdraw), d.withdraw ? 3 : 0)} rx="7.5" fill={COLORS.withdraw}>
              <title>Withdraw: {d.withdraw}</title>
            </rect>
            <rect x={cx + 2} y={y(d.deposit)} width="15" height={Math.max(B - y(d.deposit), d.deposit ? 3 : 0)} rx="7.5" fill={COLORS.deposit}>
              <title>Deposit: {d.deposit}</title>
            </rect>
            <text x={cx} y={B + 30} textAnchor="middle">{d.label}</text>
          </g>
        );
      })}
    </svg>
  );
}

/* ---------- Pie: депозити vs зняття ---------- */
export function SplitChart({ deposit, withdraw }) {
  const total = deposit + withdraw;
  if (!total) return <Empty>No transactions yet</Empty>;

  const slices = [
    { label: "Deposits", value: deposit, color: COLORS.deposit },
    { label: "Withdrawals", value: withdraw, color: COLORS.withdraw },
  ].filter((s) => s.value > 0);

  const r = 118;
  let a0 = -Math.PI / 2;

  return (
    <svg className="chart pie" viewBox="-130 -130 260 260" role="img" aria-label="Deposits versus withdrawals">
      {slices.map((s) => {
        const frac = s.value / total;
        const a1 = a0 + frac * 2 * Math.PI;
        const mid = (a0 + a1) / 2;
        const off = slices.length > 1 ? 6 : 0; // розсунуті сектори, як в еталоні
        const ox = Math.cos(mid) * off, oy = Math.sin(mid) * off;
        const pt = (a) => [Math.cos(a) * r + ox, Math.sin(a) * r + oy];
        const [x0, y0] = pt(a0), [x1, y1] = pt(a1);
        const lx = Math.cos(mid) * r * (slices.length > 1 ? 0.6 : 0) + ox;
        const ly = Math.sin(mid) * r * (slices.length > 1 ? 0.6 : 0) + oy;
        a0 = a1;
        return (
          <g key={s.label}>
            {slices.length === 1 ? (
              <circle r={r} fill={s.color} />
            ) : (
              <path d={`M${ox} ${oy}L${x0} ${y0}A${r} ${r} 0 ${frac > 0.5 ? 1 : 0} 1 ${x1} ${y1}Z`} fill={s.color} />
            )}
            <text x={lx} y={ly - 2} textAnchor="middle" className="pie-pct">{Math.round(frac * 100)}%</text>
            <text x={lx} y={ly + 14} textAnchor="middle" className="pie-label">{s.label}</text>
          </g>
        );
      })}
    </svg>
  );
}

/* ---------- Balance History: плавна лінія ---------- */
export function BalanceChart({ points }) {
  if (points.length < 2) return <Empty>Make a deposit to see your balance history</Empty>;

  const values = points.map((p) => p.value);
  const { max, step } = niceScale(Math.max(...values, 0));
  const L = 56, R = 548, T = 14, B = 216;
  const x = (i) => L + 20 + (i * (R - L - 40)) / (points.length - 1);
  const y = (v) => B - (Math.max(v, 0) / max) * (B - T);
  const pts = points.map((p, i) => [x(i), y(p.value)]);

  // Catmull-Rom → Bézier
  let d = `M${pts[0][0]} ${pts[0][1]}`;
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[i - 1] || pts[i], p1 = pts[i], p2 = pts[i + 1], p3 = pts[i + 2] || p2, k = 0.18;
    d += `C${p1[0] + (p2[0] - p0[0]) * k} ${p1[1] + (p2[1] - p0[1]) * k} ${p2[0] - (p3[0] - p1[0]) * k} ${p2[1] - (p3[1] - p1[1]) * k} ${p2[0]} ${p2[1]}`;
  }

  const ticks = [];
  for (let v = 0; v <= max; v += step) ticks.push(v);
  const every = Math.ceil(points.length / 7);

  return (
    <svg className="chart" viewBox="0 0 560 260" role="img" aria-label="Balance history">
      <defs>
        <linearGradient id="balFill" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#2d60ff" stopOpacity=".25" />
          <stop offset="1" stopColor="#2d60ff" stopOpacity="0" />
        </linearGradient>
      </defs>
      {ticks.map((v) => (
        <g key={v}>
          <line x1={L} x2={R} y1={y(v)} y2={y(v)} stroke="#f0f1f7" strokeDasharray="3 4" />
          <text x={L - 12} y={y(v) + 4} textAnchor="end">{compact(v)}</text>
        </g>
      ))}
      {points.map((p, i) =>
        i % every === 0 ? <text key={i} x={x(i)} y={B + 28} textAnchor="middle">{p.label}</text> : null
      )}
      <path d={`${d}L${pts.at(-1)[0]} ${B}L${pts[0][0]} ${B}Z`} fill="url(#balFill)" />
      <path d={d} fill="none" stroke="#1814f3" strokeWidth="3" strokeLinecap="round" />
      {pts.map(([px, py], i) => (
        <circle key={i} cx={px} cy={py} r="3.5" fill="#fff" stroke="#1814f3" strokeWidth="2">
          <title>{points[i].label}: {points[i].value}</title>
        </circle>
      ))}
    </svg>
  );
}

/* ---------- Sparkline: міні-графік ціни ---------- */
export function Sparkline({ values, up }) {
  const w = 100, h = 32;
  const min = Math.min(...values), rng = Math.max(...values) - min || 1;
  const pts = values.map((v, i) => `${(i / (values.length - 1)) * w},${h - 2 - ((v - min) / rng) * (h - 4)}`).join(" ");
  return (
    <svg viewBox={`0 0 ${w} ${h}`} width="100" height="32" aria-hidden="true">
      <polyline points={pts} fill="none" stroke={up ? "#16b3a6" : "#ff4b4a"} strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />
    </svg>
  );
}
