import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";

import Login from "./components/Login";
import Register from "./components/Register";
import Bank from "./components/Bank";
import AdminPanel from "./components/AdminPanel";

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/" />;
};

const AdminRoute = ({ children }) => {
  const token = localStorage.getItem("token");

  if (!token) return <Navigate to="/" />;

  // role в JWT це масив ["ROLE_ADMIN"], тому порівняння payload.role !== "ADMIN" завжди давало редірект
  let isAdmin = false;
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    isAdmin = Array.isArray(payload.role) && payload.role.includes("ROLE_ADMIN");
  } catch {
    return <Navigate to="/" />;
  }

  if (!isAdmin) {
    return <Navigate to="/bank" />;
  }

  return children;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
          path="/bank"
          element={
            <PrivateRoute>
              <Bank />
            </PrivateRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminPanel />
            </AdminRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;