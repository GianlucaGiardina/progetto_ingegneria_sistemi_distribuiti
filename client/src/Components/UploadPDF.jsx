import React, { useState, useRef } from "react";
import { Spinner } from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import Cookies from "js-cookie";
import { calculateFileHash } from "../utils/file";

export const UploadPDF = ({ className }) => {
  const [isSending, setIsSending] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isDone, setIsDone] = useState(false);
  const [file, setFile] = useState(null);
  const [fileHash, setFileHash] = useState(null);
  const [fileType, setFileType] = useState(null);
  const [summarization, setSummarization] = useState(false);
  const [nlp, setNlp] = useState(false);
  const [context, setContext] = useState(false);
  const [services, setServices] = useState([]);
  const [request_id, setRequestId] = useState(null);
  const user_id = Cookies.get("username");

  const fileInputRef = useRef(null);

  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];

    if (selectedFile) {
      const allowedTypes = ["application/pdf", "image/png", "image/jpeg"];

      if (allowedTypes.includes(selectedFile.type)) {
        const reader = new FileReader();

        reader.onloadend = () => {
          setFile(reader.result);
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
    setIsSending(true);
    setIsDone(false);
    setIsProcessing(false);

    const endpoint =
      fileType === "pdf"
        ? process.env["UPLOAD_PDF_URL"]
        : process.env["UPLOAD_IMAGE_URL"];

    try {
      console.log(endpoint);
      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          file: file,
          userId: user_id,
          fileId: fileHash,
          services: services,
        }),
      });

      const data = await response.json();
      setIsSending(false);

      if (data.success) {
        setIsDone(true);
      } else {
        setIsProcessing(true);
        pollForProcessingStatus();
      }
      //verificare se è corretto
      setRequestId(data.request_id);
    } catch (error) {
      console.error(error);
      setIsSending(false);
    }
  };

  const pollForProcessingStatus = async () => {
    const interval = setInterval(async () => {
      try {
        const response = await fetch(
          `${process.env["STATUS_URL"]}?requestId=${request_id}`
        );
        const data = await response.json();

        // Verificare cosa ritorna il server
        if (data.status === "done") {
          setIsProcessing(false);
          setIsDone(true);
          clearInterval(interval);
        }
      } catch (error) {
        console.error(error);
      }
    }, 5000);
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
              {(isProcessing || isDone) && (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  fill="currentColor"
                  className={`bi bi-circle-fill ${
                    isProcessing ? "text-gray-400" : ""
                  } ${isDone ? "text-green-400" : ""}`}
                  viewBox="0 0 16 16"
                >
                  <circle cx="8" cy="8" r="8" />
                </svg>
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
