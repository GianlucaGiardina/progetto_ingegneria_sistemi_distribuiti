import React, { useRef, useState } from "react";

export const Login = () => {
  const username = useRef(null);
  const password = useRef(null);
  const [error, setError] = useState(null);

  const login = async (e) => {
    e.preventDefault();
    const user = username.current.value;
    const pass = password.current.value;

    if (user && pass) {
      try {
        const response = await fetch("http://localhost:3001/login", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ username: user, password: pass }),
        });
        const data = await response.json();
        if (data.error) {
          setError(data.error);
          return;
        }
      } catch (error) {
        console.error(error);
      }
    }
  };
  return (
    <>
      <div className="bg-lime-400 w-full h-full flex items-center justify-center">
        <div className="bg-white w-[448px] h-80 rounded-lg shadow-2xl flex flex-col items-center justify-center">
          <form className="flex flex-col items-center justify-center space-y-4">
            <input
              ref={username}
              type="text"
              placeholder="Username"
              className="w-80 p-2 border border-gray-300 rounded-md mb-4"
            />
            <input
              ref={password}
              type="password"
              placeholder="Password"
              className="w-80 p-2 border border-gray-300 rounded-md mb-4"
            />
            <button
              className="w-80 p-2 bg-lime-600 text-white font-bold rounded-md"
              onClick={login}
            >
              Login
            </button>
          </form>
          {error && (
            <span className="text-red-500">Username e/o password errata</span>
          )}
          <span className="font-light text-sm mt-4">
            Non sei ancora registrato? <a href="/register">Crea un account</a>
          </span>
        </div>
      </div>
    </>
  );
};
