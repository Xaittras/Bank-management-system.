// Рівень клієнта рахується із сумарного балансу всіх рахунків
export const TIERS = [
  { name: "Standard", min: 0, rateDiscount: 0, perks: ["Deposits and withdrawals", "Transaction history", "Standard loan rates"] },
  { name: "Silver", min: 1000, rateDiscount: 0.5, perks: ["Loan rate −0.5 pp", "Priority support chat", "Unlimited accounts"] },
  { name: "Gold", min: 10000, rateDiscount: 1, perks: ["Loan rate −1 pp", "Early access to investments", "Monthly statement review"] },
  { name: "Platinum", min: 50000, rateDiscount: 2, perks: ["Loan rate −2 pp", "Personal manager", "Custom limits"] },
];

export function tierFor(total) {
  let index = 0;
  TIERS.forEach((t, i) => { if (total >= t.min) index = i; });
  const tier = TIERS[index];
  const next = TIERS[index + 1] ?? null;
  const progress = next ? (total - tier.min) / (next.min - tier.min) : 1;
  return { tier, index, next, progress, total };
}
