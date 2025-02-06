import React from "react";
import { useState } from "react";
import { Spinner } from "react-bootstrap";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";

export const UploadPDF = ({ className }) => {
  const [isSending, setIsSending] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isDone, setIsDone] = useState(false);

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
          <Button variant="primary" type="submit">
            Invia
          </Button>
        </Form>
      </div>
    </>
  );
};
