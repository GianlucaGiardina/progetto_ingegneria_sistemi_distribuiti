import React, { createContext, useState } from "react";
import { Requests, UploadPDF } from "../";

export const UploadContext = createContext(null);

export const Dashboard = () => {
  const [uploadSignal, setUploadSignal] = useState(false);

  return (
    <>
      <h1 className="w-screen text-center">Dashboard</h1>
      <div className="grid grid-cols-2 gap-1 mx-10">
        <UploadContext.Provider value={{ uploadSignal, setUploadSignal }}>
          <div>
            <UploadPDF className="w-96 flex mt-40" />
          </div>
          <div>
            <Requests />
          </div>
        </UploadContext.Provider>
      </div>
    </>
  );
};
