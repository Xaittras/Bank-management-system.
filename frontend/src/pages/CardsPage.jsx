import BankCard from "../components/BankCard";
import NewAccountForm from "../components/NewAccountForm";

export default function CardsPage({ ctx }) {
  const { accounts, holder, selectedAccountId, setSelectedAccountId } = ctx;
  return (
    <main>
      <section className="full">
        <h2>Credit Cards</h2>
        <div className="cards-grid">
          {accounts.map((acc, i) => (
            <BankCard key={acc.id} account={acc} holder={holder} blue={i % 2 === 0}
              selected={acc.id === selectedAccountId} onSelect={setSelectedAccountId} />
          ))}
          <NewAccountForm ctx={ctx} />
        </div>
      </section>
    </main>
  );
}
