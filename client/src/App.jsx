import "./App.css";
import { UploadPDF } from "./Components";

function App() {
  return (
    <>
      <div className="w-full h-full flex items-center justify-center">
        <UploadPDF className="w-96" />
      </div>
    </>
  );
}

export default App;
