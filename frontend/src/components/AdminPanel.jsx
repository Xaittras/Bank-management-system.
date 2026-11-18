import Layout from "./Layout";

const NAV = [{ to: "/bank", end: true, label: "Users", icon: "users" }];

function AdminPanel({ users = [], logout, email, onRefresh }) {
  return (
    <Layout title="Admin Panel" nav={NAV} email={email} onLogout={logout} onRefresh={onRefresh}>
      <main id="users">
        <section className="full">
          <h2>All users <span className="count">{users.length}</span></h2>
          <div className="panel table-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th></tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>#{user.id}</td>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td><span className={`badge ${user.role === "ADMIN" ? "admin" : ""}`}>{user.role}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!users.length && <p className="empty">No users</p>}
          </div>
        </section>
      </main>
    </Layout>
  );
}

export default AdminPanel;
