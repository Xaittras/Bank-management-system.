import { useNavigate } from "react-router-dom";
import NewAccountForm from "../components/NewAccountForm";
import Icon from "../components/Icons";
import { money } from "../lib/stats";

export default function AccountsPage({ ctx }) {
  const { accounts, selectedAccountId, setSelectedAccountId } = ctx;
  const navigate = useNavigate();
  const total = accounts.reduce((s, a) => s + Number(a.balance), 0);

  return (
    <main>
      <section className="full stat-row">
        <div className="panel stat"><span className="ic dep"><Icon name="wallet" /></span>
          <div><small>Total balance</small><b>{money(total)}</b></div></div>
        <div className="panel stat"><span className="ic wd"><Icon name="card" /></span>
          <div><small>Accounts</small><b>{accounts.length}</b></div></div>
      </section>

      <section className="full">
        <h2>My Accounts</h2>
        <div className="panel table-wrap">
          {accounts.length ? (
            <table>
              <thead><tr><th>ID</th><th>Account number</th><th className="num">Balance</th><th /></tr></thead>
              <tbody>
                {accounts.map((a) => (
                  <tr key={a.id}>
                    <td>#{a.id} {a.id === selectedAccountId && <span className="badge">Selected</span>}</td>
                    <td>{a.accountNumber}</td>
                    <td className="num">{money(a.balance)}</td>
                    <td className="num">
                      <button className="link" onClick={() => { setSelectedAccountId(a.id); navigate("/bank"); }}>Open</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="empty">You have no accounts yet</p>
          )}
        </div>
      </section>

      <section className="full">
        <h2>Open new account</h2>
        <div className="cards-row"><NewAccountForm ctx={ctx} /></div>
      </section>
    </main>
  );
}
