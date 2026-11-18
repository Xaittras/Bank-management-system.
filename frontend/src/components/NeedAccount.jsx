import { Link } from "react-router-dom";

export default function NeedAccount({ what }) {
  return (
    <main>
      <section className="full">
        <div className="panel soon">
          <h2>No account yet</h2>
          <p>Open an account to {what}.</p>
          <Link className="btn primary narrow" to="/bank/accounts">Open an account</Link>
        </div>
      </section>
    </main>
  );
}
