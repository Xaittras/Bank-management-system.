function AdminPanel({ users = [], logout }) {
  return (
    <div className="box">
      <h3>🛠 Admin Panel</h3>

      <button onClick={() => window.confirm("Logout?") && logout()}>
        🚪 Logout
      </button>

      <hr />

      <h4>All users</h4>

      {users.map(user => (
        <div
          key={user.id}
          style={{
            padding: "12px",
            borderRadius: "10px",
            border: "1px solid #ccc",
            marginBottom: "10px",
            background: "#f5f5f5"
          }}
        >
          <p><b>ID:</b> {user.id}</p>
          <p><b>Email:</b> {user.email}</p>
          <p><b>Role:</b> {user.role}</p>
        </div>
      ))}
    </div>
  );
}

export default AdminPanel;