import Icon from "./Icons";

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="auth">
      <div className="auth-card">
        <div className="logo"><Icon name="card" size={32} />BankDash.</div>
        <h1>{title}</h1>
        <p className="sub">{subtitle}</p>
        {children}
        <p className="auth-foot">{footer}</p>
      </div>
    </div>
  );
}
