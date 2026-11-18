import Icon from "./Icons";

export default function NewAccountForm({ ctx }) {
  const { accounts, newAccountName, setNewAccountName, createAccount } = ctx;
  return (
    <form className="cc new" onSubmit={createAccount}>
      <Icon name="plus" size={28} />
      <b>{accounts.length ? "New account" : "Create your first account"}</b>
      <input
        placeholder="Account name"
        value={newAccountName}
        maxLength={100}
        onChange={(e) => setNewAccountName(e.target.value)}
        aria-label="Account name"
      />
      <button className="btn primary" type="submit">Create account</button>
    </form>
  );
}
