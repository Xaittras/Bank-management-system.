export const round2 = (x) => Math.round(x * 100) / 100;

export const OFFERS = [
  { id: "personal", name: "Personal loan", rate: 12, min: 500, max: 20000, minTerm: 6, maxTerm: 60 },
  { id: "auto", name: "Auto loan", rate: 9, min: 2000, max: 50000, minTerm: 12, maxTerm: 84 },
  { id: "education", name: "Education loan", rate: 6, min: 1000, max: 30000, minTerm: 12, maxTerm: 120 },
];

// Ануїтетний щомісячний платіж
export const monthly = (amount, ratePct, months) => {
  const r = ratePct / 1200;
  return r === 0 ? amount / months : (amount * r) / (1 - (1 + r) ** -months);
};

// Повний графік платежів; останній платіж закриває залишок
export function buildSchedule(loan) {
  const r = loan.rate / 1200;
  const pay = round2(monthly(loan.principal, loan.rate, loan.term));
  let balance = loan.principal;
  const rows = [];
  for (let n = 1; n <= loan.term; n++) {
    const interest = round2(balance * r);
    const payment = n === loan.term ? round2(balance + interest) : pay;
    const principal = round2(payment - interest);
    balance = Math.max(round2(balance - principal), 0);
    const date = new Date(loan.start);
    date.setMonth(date.getMonth() + n);
    rows.push({ n, date: date.toISOString(), payment, interest, principal, balance });
  }
  return rows;
}
