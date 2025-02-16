import React, { useState, useRef, useContext } from "react";
import { Spinner } from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import Cookies from "js-cookie";
import { calculateFileHash } from "../utils/file";
import { UploadContext } from "./Pages/Dashboard";

export const UploadPDF = ({ className }) => {
  const [isSending, setIsSending] = useState(false);
  const [file, setFile] = useState(null);
  const [fileName, setFileName] = useState(null);
  const [fileHash, setFileHash] = useState(null);
  const [fileType, setFileType] = useState(null);
  const [summarization, setSummarization] = useState(false);
  const [nlp, setNlp] = useState(false);
  const [context, setContext] = useState(false);
  const [services, setServices] = useState([]);
  const [requestId, setRequestId] = useState(null);
  const user_id = Cookies.get("username");
  const { uploadSignal, setUploadSignal } = useContext(UploadContext);

  const fileInputRef = useRef(null);

  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];

    if (selectedFile) {
      const allowedTypes = ["application/pdf", "image/png", "image/jpeg"];

      if (allowedTypes.includes(selectedFile.type)) {
        const reader = new FileReader();

        reader.onloadend = () => {
          const base64 =
            typeof reader.result === "string"
              ? reader.result.split(",")[1]
              : "";
          setFile(base64);
          setFileName(selectedFile.name);
          setFileType(
            selectedFile.type === "application/pdf" ? "pdf" : "image"
          );
        };

        reader.readAsDataURL(selectedFile);
        calculateFileHash(selectedFile).then((hash) => setFileHash(hash));
        setFileHash(calculateFileHash(selectedFile));
      } else {
        alert(
          "Formato file non supportato! Seleziona un PDF, TXT o un'immagine (PNG/JPEG)."
        );
        setFile(null);
        setFileHash(null);
        setFileType(null);

        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      alert("Seleziona un file valido!");
      return;
    }

    if (services.length === 0) {
      alert("Seleziona almeno un servizio!");
      return;
    }

    setIsSending(true);

    const endpoint =
      fileType === "pdf"
        ? process.env["UPLOAD_PDF_URL"]
        : process.env["UPLOAD_IMAGE_URL"];

    try {
      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          fileName: fileName,
          file: file,
          userId: user_id,
          fileId: fileHash,
          services: services,
        }),
      });

      const data = await response.json();
      setRequestId(data.requestId);
      setIsSending(false);
      setUploadSignal(true);
      handleClear(e);
    } catch (error) {
      console.error(error);
      setIsSending(false);
    }
  };

  const handleClear = async (e) => {
    e.preventDefault();
    setFile(null);
    setFileType(null);
    fileInputRef.current.value = "";
    setSummarization(false);
    setNlp(false);
    setContext(false);
    setServices([]);
  };

  return (
    <>
      <div className={className}>
        <Form>
          <Form.Group className="mb-3" controlId="formFile">
            <Form.Label>Inserisci un file</Form.Label>
            <div className="flex items-center">
              <Form.Control
                type="file"
                placeholder="file"
                className="max-w-80 min-w-80 me-2"
                onChange={handleFileChange}
                ref={fileInputRef}
              />
              {isSending && (
                <Spinner animation="border" role="status" size="sm" />
              )}
            </div>
          </Form.Group>

          {fileType === "pdf" && (
            <>
              <Form.Check
                type="checkbox"
                label="Summarization"
                className="mb-2"
                checked={summarization}
                onChange={(e) => {
                  setSummarization(e.target.checked);
                  setServices([...services, "summarization"]);
                }}
              />
              <Form.Check
                type="checkbox"
                label="NLP"
                className="mb-3"
                checked={nlp}
                onChange={(e) => {
                  setNlp(e.target.checked);
                  setServices([...services, "nlp"]);
                }}
              />
            </>
          )}
          {fileType === "image" && (
            <Form.Check
              type="checkbox"
              label="Context"
              className="mb-3"
              checked={context}
              onChange={(e) => {
                setContext(e.target.checked);
                setServices([...services, "context"]);
              }}
            />
          )}

          <div className="max-w-80 min-w-80 flex justify-between">
            <Button variant="danger" className="me-2" onClick={handleClear}>
              Rimuovi
            </Button>
            <Button variant="primary" type="submit" onClick={handleSubmit}>
              Invia
            </Button>
          </div>
        </Form>
      </div>
    </>
  );
};
