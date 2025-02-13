import React from "react";
import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { Dashboard, Login, Register } from "./Components/Pages";
import { useState } from "react";
import { useEffect } from "react";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

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
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkTokenValidity();
  }, []);

  return (
    <>
      <div className="w-lvw h-lvh">
        <BrowserRouter>
          <Routes>
            <Route
              path="/"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Dashboard />
                  ) : (
                    <Navigate to="/login" />
                  )
                ) : (
                  <div></div>
                )
              }
            />
            <Route
              path="/dashboard"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Dashboard />
                  ) : (
                    <Navigate to="/login" />
                  )
                ) : (
                  <div></div>
                )
              }
            />
            <Route
              path="/login"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Navigate to="/" />
                  ) : (
                    <Login />
                  )
                ) : (
                  <div></div>
                )
              }
            />
            <Route
              path="/register"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Navigate to="/" />
                  ) : (
                    <Register />
                  )
                ) : (
                  <div></div>
                )
              }
            />
          </Routes>
        </BrowserRouter>
      </div>
    </>
  );
}

export default App;
