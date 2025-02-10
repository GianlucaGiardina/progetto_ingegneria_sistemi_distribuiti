import React from "react";
import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Dashboard, Login, Register } from "./Components/Pages";
import { useState } from "react";
import { useEffect } from "react";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const checkTokenValidity = async () => {
    try {
      const response = await fetch("http://localhost:3001/api/validate_token", {
        method: "GET",
        credentials: "include",
      });
      const data = await response.json();
      if (data.valid) {
        setIsLoggedIn(true);
      }
    } catch {
      setIsLoggedIn(false);
    }
  };

  useEffect(() => {
    checkTokenValidity();
  }, []);

  return (
    <>
      <div className="w-lvw h-lvh flex items-center justify-center">
        <BrowserRouter>
          <Routes>
            <Route path="/" element={isLoggedIn ? <Dashboard /> : <Login />} />
            <Route
              path="/login"
              element={isLoggedIn ? <Dashboard /> : <Login />}
            />
            <Route
              path="/register"
              element={isLoggedIn ? <Dashboard /> : <Register />}
            />
          </Routes>
        </BrowserRouter>
        {/* <UploadPDF className="w-96" /> */}
      </div>
    </>
  );
}

export default App;
