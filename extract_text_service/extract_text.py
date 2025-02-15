import base64
import io
import re
from flask import Flask, request, jsonify
from pdfminer.high_level import extract_text

app = Flask(__name__)

def clean_text(text):
    """Rimuove spazi inutili, nuove righe multiple e caratteri speciali."""
    text = text.replace("\n", " ")  # Sostituisce \n con spazio
    text = text.replace("\t", " ")  # Sostituisce \t con spazio
    text = re.sub(r'\s+', ' ', text)  # Rimuove spazi multipli
    return text.strip()

@app.route('/extract/text', methods=['POST'])
def extract_text_from_pdf():
    print(request.get_json())
    # try:
        # Controlla il tipo di contenuto
    if request.content_type != "application/json":
        return jsonify({"error": "Unsupported Media Type"}), 415

    # Decodifica il JSON ricevuto
    file_data = request.get_json()
    if "file" not in file_data:
        return jsonify({"error": "File key missing in JSON"}), 400

    # Decodifica il file Base64 ricevuto
    file_content = base64.b64decode(file_data["file"])
    pdf_stream = io.BytesIO(file_content)

    # Estrai il testo
    extracted_text = extract_text(pdf_stream)

    # Pulisce il testo estratto
    cleaned_text = clean_text(extracted_text)

    return jsonify({"extracted_text": cleaned_text})
    
    # except Exception as e:
    #     print(str(e))
    #     return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
