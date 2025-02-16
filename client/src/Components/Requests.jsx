import React, { useContext, useEffect, useState, useRef } from "react";
import { Table, Spinner } from "react-bootstrap";
import Cookies from "js-cookie";
import { UploadContext } from "./Pages/Dashboard";

export const Requests = () => {
  const [requests, setRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(new Set());
  const user_id = Cookies.get("username");
  const { uploadSignal, setUploadSignal } = useContext(UploadContext);
  const pollingRef = useRef(null);

  const fetchRequests = () => {
    const endpoint = `${process.env["STATUS_BY_USER_URL"]}?userId=${user_id}`;
    fetch(endpoint, { method: "GET" })
      .then((response) => response.json())
      .then((data) => {
        const reversedData = Array.isArray(data) ? data.slice().reverse() : [];
        setRequests(reversedData);

        if (uploadSignal) {
          const newRequestId = reversedData[0]?.requestId;
          if (newRequestId) {
            setLoadingRequests((prev) => {
              const newSet = new Set(prev);
              newSet.add(newRequestId);
              return newSet;
            });

            setTimeout(() => {
              setLoadingRequests((prev) => {
                const newSet = new Set(prev);
                newSet.delete(newRequestId);
                return newSet;
              });
            }, 6000);
          }
        }
      })
      .catch((error) => console.error("Errore nel fetch:", error));
  };

  useEffect(() => {
    fetchRequests();

    if (!pollingRef.current) {
      pollingRef.current = setInterval(fetchRequests, 10000);
    }

    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (uploadSignal) {
      fetchRequests();
      setUploadSignal(false);
    }
  }, [uploadSignal]);

  return (
    <div className="mt-40 h-96 overflow-y-scroll">
      <Table striped bordered hover>
        <thead>
          <tr>
            <th>Request Id</th>
            <th className="w-5">File Name</th>
            <th>Summarization</th>
            <th>NLP</th>
            <th>Context</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((request) => (
            <tr key={request.requestId}>
              <td>{request.requestId}</td>
              <td>
                {request.services.includes("summarization") ||
                request.services.includes("nlp") ? (
                  <a href={`/results/text-extraction/${request.requestId}`}>
                    {request.fileName}
                  </a>
                ) : (
                  request.fileName
                )}
              </td>
              <td>
                {loadingRequests.has(request.requestId) ? (
                  <Spinner animation="border" size="sm" variant="secondary" />
                ) : request.serviceStatuses.summarization ? (
                  request.serviceStatuses.summarization === "completed" ? (
                    <div>
                      <span className="w-3 h-3 bg-green-500 rounded-full inline-block mr-2"></span>
                      <a
                        href={`/results/summarization/${request.requestId}`}
                        className="text-blue-500 -mt-2.5"
                      >
                        Visualizza
                      </a>
                    </div>
                  ) : (
                    <span className="w-3 h-3 bg-blue-500 rounded-full inline-block animate-pulse duration-2000 ease-in-out"></span>
                  )
                ) : (
                  "Non richiesto"
                )}
              </td>
              <td>
                {loadingRequests.has(request.requestId) ? (
                  <Spinner animation="border" size="sm" variant="secondary" />
                ) : request.serviceStatuses.nlp ? (
                  request.serviceStatuses.nlp === "completed" ? (
                    <div>
                      <span className="w-3 h-3 bg-green-500 rounded-full inline-block mr-2"></span>
                      <a
                        href={`/results/nlp/${request.requestId}`}
                        className="text-blue-500 -mt-2.5"
                      >
                        Visualizza
                      </a>
                    </div>
                  ) : (
                    <span className="w-3 h-3 bg-blue-500 rounded-full inline-block animate-pulse duration-2000 ease-in-out"></span>
                  )
                ) : (
                  "Non richiesto"
                )}
              </td>
              <td>
                {loadingRequests.has(request.requestId) ? (
                  <Spinner animation="border" size="sm" variant="secondary" />
                ) : request.serviceStatuses.context ? (
                  request.serviceStatuses.context === "completed" ? (
                    <div>
                      <span className="w-3 h-3 bg-green-500 rounded-full inline-block mr-2"></span>
                      <a
                        href={`/results/context/${request.requestId}`}
                        className="text-blue-500 -mt-2.5"
                      >
                        Visualizza
                      </a>
                    </div>
                  ) : (
                    <span className="w-3 h-3 bg-blue-500 rounded-full inline-block animate-pulse duration-2000 ease-in-out"></span>
                  )
                ) : (
                  "Non richiesto"
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </div>
  );
};
