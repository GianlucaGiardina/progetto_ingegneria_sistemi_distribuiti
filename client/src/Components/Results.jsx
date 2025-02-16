import React, { useState } from "react";
import { useEffect } from "react";
import { Table } from "react-bootstrap";
import { useParams } from "react-router-dom";

export const Results = ({ service }) => {
  const { requestId } = useParams();
  const [results, setResults] = useState(null);
  const fetchResults = () => {
    const endpoint = process.env["RESULTS_URL"];
    fetch(`${endpoint}?requestId=${requestId}`, {
      method: "GET",
    })
      .then((response) => response.json())
      .then((data) => {
        data = data[0];
        if (service === "text-extraction") {
          const res = JSON.parse(data.extractedText).extracted_text;
          setResults(res);
        } else if (service === "summarization") {
          const res = data.summarizationResult;
          const cleanedRes = res.replace('"text":" extracted_text: "', "");
          setResults(cleanedRes);
        } else if (service === "nlp") {
          const unstructuredRes = data.nlpResult;
          const regex = /\(\s*'([^']*)'\s*,\s*'([^']*)'\s*\)/g;
          let match;
          const res = [];
          while ((match = regex.exec(unstructuredRes)) !== null) {
            res.push([match[1], match[2]]);
          }
          setResults(res);
        } else if (service === "context") {
          const res = data.contextResult;
          setResults(res);
        }
      });
  };

  useEffect(() => {
    fetchResults();
  }, []);

  if (
    service === "text-extraction" ||
    service === "summarization" ||
    service === "context"
  ) {
    return (
      <>
        <h1 className="w-full text-center">
          {service === "text-extraction"
            ? "Testo"
            : service === "summarization"
            ? "Riassunto"
            : service === "context"
            ? "Context"
            : ""}
        </h1>
        <div className="min-w-full absolute flex justify-center">
          <div className="max-w-fit border-2 mt-2 p-2">{results}</div>
        </div>
      </>
    );
  }
  if (service === "nlp") {
    return (
      <div className="flex justify-center pt-10 h-[55rem]">
        <Table
          striped
          bordered
          hover
          responsive
          className="shadow-sm rounded-lg min-w-7xl max-w-7xl"
        >
          <thead className="bg-gray-200">
            <tr>
              <th className="p-2">Entità</th>
              <th className="p-2">Tipo</th>
            </tr>
          </thead>
          <tbody>
            {results
              ? results.map(([entity, type], index) => (
                  <tr key={index}>
                    <td className="p-2">{entity}</td>
                    <td className="p-2">{type}</td>
                  </tr>
                ))
              : null}
          </tbody>
        </Table>
      </div>
    );
  }
};
