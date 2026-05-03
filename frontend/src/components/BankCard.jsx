function BankCard({ account }) {
  return (
    <div>
      <p>ID: {account.id}</p>
      <p>Balance: {account.balance}</p>
    </div>
  );
}
 
export default BankCard;