import React from "react";
import { UploadPDF } from "../";

export const Dashboard = () => {
  return (
    <div className="grid grid-cols-2 gap-1">
      <div>
        <UploadPDF className="w-96 flex ml-10 mt-40" />
      </div>
      <div>
        <h1>Dashboard</h1>
      </div>
    </div>
  );
};
