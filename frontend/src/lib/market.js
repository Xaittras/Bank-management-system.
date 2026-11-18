// Демо-ринок: ціни детерміновані (залежать лише від дати), тому однакові після перезавантаження
export const ASSETS = [
  { symbol: "AAPL", name: "Apple Inc.", base: 228, seed: 1 },
  { symbol: "MSFT", name: "Microsoft", base: 415, seed: 2 },
  { symbol: "TSLA", name: "Tesla", base: 245, seed: 3 },
  { symbol: "BTC", name: "Bitcoin", base: 68000, seed: 4 },
  { symbol: "SPY", name: "S&P 500 ETF", base: 560, seed: 5 },
  { symbol: "GOLD", name: "Gold (oz)", base: 2650, seed: 6 },
];

const round2 = (x) => Math.round(x * 100) / 100;
const dayIndex = () => Math.floor(Date.now() / 86400000);
const priceOn = (a, day) =>
  round2(a.base * (1 + 0.06 * Math.sin(day / 4 + a.seed) + 0.03 * Math.sin(day / 1.7 + a.seed * 2)));

export function quote(asset, days = 30) {
  const today = dayIndex();
  const series = Array.from({ length: days }, (_, i) => priceOn(asset, today - (days - 1 - i)));
  const price = series.at(-1);
  return { price, change: (price / series.at(-2) - 1) * 100, series };
}
