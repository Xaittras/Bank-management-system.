    import React, { useState } from "react";
import { authApi } from "../api/api";
import { useNavigate } from "react-router-dom";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleRegister = async () => {
    if (!name || !email || !password) {
      alert("Заповніть усі поля");
      return;
    }
    try {
     await authApi.post("/register", {
     
  password: password,
  name: name,
  email: email
});
    

    alert("Реєстрація успішна!");
    navigate("/");
  } catch (err) {
    console.log(err.response?.data);
    alert("Помилка реєстрації");
  }
};

  return (
    <div className="box">
      <h3>🧾 Реєстрація</h3>
      <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} />
      <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
      <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
      <button onClick={handleRegister}>Зареєструватися</button>
      <p>
        Вже є акаунт? <button onClick={() => navigate("/")}>Увійти</button>
      </p>
    </div>
  );
}

export default Register;



