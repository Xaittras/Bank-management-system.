export default function SettingsPage({ ctx }) {
  const { auth, logout } = ctx;
  const rows = [["Email", auth.email], ["User ID", auth.userId], ["Role", auth.role]];
  return (
    <main>
      <section className="full">
        <h2>Profile</h2>
        <div className="panel kv">
          {rows.map(([k, v]) => (
            <div key={k}><span>{k}</span><b>{v}</b></div>
          ))}
        </div>
      </section>
      <section className="full">
        <button className="btn ghost narrow" onClick={() => window.confirm("Logout?") && logout()}>Log out</button>
      </section>
    </main>
  );
}
