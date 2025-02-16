import React from "react";
import { useRef } from "react";

export const Register = () => {
  const username = useRef(null);
  const password = useRef(null);
  const confirmPassword = useRef(null);

  const register = async (e) => {
    e.preventDefault();

    const passwordValue = password.current.value;
    const confirmPasswordValue = confirmPassword.current.value;

    if (passwordValue !== confirmPasswordValue) {
      alert("Le password non corrispondono");
      return;
    }

    const endpoint = process.env["REGISTER_URL"];
    const response = await fetch(endpoint, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: username.current.value,
        password: passwordValue,
      }),
    });

    const data = await response.json();
    if (data.error) {
      alert(data.error);
      return;
    }

    alert("Registrazione avvenuta con successo");
  };

  return (
    <>
      <div className="bg-lime-400 w-full h-full flex items-center justify-center">
        <div className="bg-white w-[448px] h-96 rounded-lg shadow-2xl flex flex-col items-center justify-center">
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
            <input
              ref={confirmPassword}
              type="password"
              placeholder="Conferma Password"
              className="w-80 p-2 border border-gray-300 rounded-md mb-4"
            />
            <button
              className="w-80 p-2 bg-lime-600 text-white font-bold rounded-md"
              onClick={register}
            >
              Registrati
            </button>
          </form>
          <span className="font-light text-sm mt-4">
            Sei già registrato? <a href="/login">Fai il login</a>
          </span>
        </div>
      </div>
    </>
  );
};
