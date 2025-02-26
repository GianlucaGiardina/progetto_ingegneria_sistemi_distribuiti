import React from "react";
import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { Dashboard, Login, Register } from "./Components/Pages";
import { useState } from "react";
import { useEffect } from "react";
import { Results } from "./Components/Results";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const checkTokenValidity = async () => {
    try {
      const endpoint = process.env["VALIDATE_TOKEN_URL"];
      const response = await fetch(endpoint, {
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
      <div className="w-lvw min-h-lvh max-h-fit bg-blue-100">
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
            <Route
              path="/results/text-extraction/:requestId"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Results service="text-extraction" />
                  ) : (
                    <Navigate to="/login" />
                  )
                ) : (
                  <div></div>
                )
              }
            />
            <Route
              path="/results/summarization/:requestId"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Results service="summarization" />
                  ) : (
                    <Navigate to="/login" />
                  )
                ) : (
                  <div></div>
                )
              }
            />
            <Route
              path="/results/nlp/:requestId"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Results service="nlp" />
                  ) : (
                    <Navigate to="/login" />
                  )
                ) : (
                  <div></div>
                )
              }
            />
            <Route
              path="/results/context/:requestId"
              element={
                !isLoading ? (
                  isLoggedIn ? (
                    <Results service="context" />
                  ) : (
                    <Navigate to="/login" />
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
