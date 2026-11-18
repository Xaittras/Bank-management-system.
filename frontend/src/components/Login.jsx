import React, { useState } from "react";
import { authApi, errorMessage } from "../api/api";
import { useNavigate } from "react-router-dom";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
  try {
    const res = await authApi.post("/login", { email, password });

    localStorage.setItem("token", res.data.token);
    navigate("/bank");
  }catch (err) {
  alert(errorMessage(err, "Помилка входу"));
}
};

  return (
    <div className="box">
      <h3>🔐 Вхід</h3>
      <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
      <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
      <button onClick={handleLogin}>Увійти</button>
      <p>
        Немає акаунту? <button onClick={() => navigate("/register")}>Реєстрація</button>
      </p>
    </div>
  );
}

export default Login;