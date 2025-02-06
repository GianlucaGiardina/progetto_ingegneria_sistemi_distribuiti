import base64
import io
from flask import Flask, request, jsonify
from pdfminer.high_level import extract_text

app = Flask(__name__)

@app.route('/apii/extract/text', methods=['POST'])
def extract_text_from_pdf():
    try:
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

        return jsonify({"extracted_text": extracted_text})
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
