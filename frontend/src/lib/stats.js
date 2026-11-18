// Усі дані для графіків рахуються з реальних транзакцій рахунку

export const COLORS = { deposit: "#16dbcc", withdraw: "#1814f3" };

export const money = (n) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(Number(n) || 0);

// "12,50" або "12.5" -> 12.5; null, якщо формат неправильний (максимум 2 знаки, > 0)
export const parseAmount = (raw) => {
  const t = String(raw ?? "").trim();
  if (!/^\d+([.,]\d{1,2})?$/.test(t)) return null;
  const n = Number(t.replace(",", "."));
  return n > 0 ? n : null;
};

export const compact = (n) => new Intl.NumberFormat("en", { notation: "compact" }).format(n);

export const fmtDate = (iso) =>
  new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "long", year: "numeric" });

const isDeposit = (t) => String(t.type).toUpperCase() === "DEPOSIT";

export const fmtDateTime = (iso) =>
  new Date(iso).toLocaleString("en-GB", { day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });

// Депозити/зняття за останні 7 днів
export function weeklyActivity(txs) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    days.push({
      key: d.toDateString(),
      label: d.toLocaleDateString("en-US", { weekday: "short" }),
      deposit: 0,
      withdraw: 0,
    });
  }
  txs.forEach((t) => {
    const day = days.find((d) => d.key === new Date(t.createdAt).toDateString());
    if (day) day[isDeposit(t) ? "deposit" : "withdraw"] += Number(t.amount);
  });
  return days;
}

export function totals(txs) {
  return txs.reduce(
    (acc, t) => {
      acc[isDeposit(t) ? "deposit" : "withdraw"] += Number(t.amount);
      return acc;
    },
    { deposit: 0, withdraw: 0 }
  );
}

// Відновлює історію балансу назад від поточного балансу
export function balanceHistory(txs, current) {
  if (!txs.length) return [];
  const sorted = [...txs].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  const signed = (t) => (isDeposit(t) ? 1 : -1) * Number(t.amount);
  let bal = Number(current) - sorted.reduce((s, t) => s + signed(t), 0);
  const points = [{ label: "Start", value: bal }];
  sorted.forEach((t) => {
    bal += signed(t);
    points.push({
      label: new Date(t.createdAt).toLocaleDateString("en-GB", { day: "numeric", month: "short" }),
      value: bal,
    });
  });
  return points.slice(-12);
}

// "Гарна" шкала осі Y: ~4 інтервали з кроком 1/2/5 × 10^k
export function niceScale(max) {
  if (!(max > 0)) return { max: 100, step: 25 };
  const raw = max / 4;
  const mag = 10 ** Math.floor(Math.log10(raw));
  const n = raw / mag;
  const step = (n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10) * mag;
  return { max: step * Math.ceil(max / step), step };
}
