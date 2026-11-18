import Icon from "./Icons";
import { money } from "../lib/stats";

function BankCard({ account, holder, selected, blue, onSelect }) {
  return (
    <button
      type="button"
      className={`cc ${blue ? "blue" : ""} ${selected ? "selected" : ""}`}
      onClick={() => onSelect(account.id)}
      aria-pressed={selected}
    >
      <div className="cc-top">
        <div>
          <small>Balance</small>
          <b className="cc-balance">{money(account.balance)}</b>
        </div>
        <Icon name="chip" size={34} />
        <div>
          <small>Card holder</small>
          <b>{holder}</b>
        </div>
        <div>
          <small>Account ID</small>
          <b>#{account.id}</b>
        </div>
      </div>
      <div className="cc-bot">
        <span>{account.accountNumber}</span>
        <span className="dots"><i /><i /></span>
      </div>
    </button>
  );
}

export default BankCard;
