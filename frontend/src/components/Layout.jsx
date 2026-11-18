import { NavLink } from "react-router-dom";
import Icon from "./Icons";

export default function Layout({ title, nav, email, onLogout, onRefresh, children }) {
  const initials = (email || "?").slice(0, 2).toUpperCase();

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="logo">
          <Icon name="card" size={32} />
          BankDash.
        </div>
        <nav className="nav">
          {nav.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}>
              <Icon name={item.icon} />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className="nav-logout" onClick={() => window.confirm("Logout?") && onLogout()}>
          <Icon name="logout" />
          Logout
        </button>
      </aside>

      <div className="content">
        <header className="topbar">
          <h1>{title}</h1>
          {onRefresh && (
            <button className="icon-btn" onClick={onRefresh} aria-label="Refresh" title="Refresh">
              <Icon name="refresh" />
            </button>
          )}
          <span className="user-email">{email}</span>
          <div className="avatar" aria-hidden="true">{initials}</div>
        </header>
        {children}
      </div>
    </div>
  );
}
