import base64
import hashlib
import requests
from flask import Flask, render_template, request, jsonify

app = Flask(__name__)

# URL del server Spring Boot
BASE_URL = "http://host.docker.internal:8080/api"
UPLOAD_PDF_URL = f"{BASE_URL}/process_pdf"
UPLOAD_IMAGE_URL = f"{BASE_URL}/process_image"
STATUS_URL = f"{BASE_URL}/status/getByUserId"
RESULTS_URL = f"{BASE_URL}/results/get"

def encode_file_to_base64(file):
    """Converte un file in Base64."""
    return base64.b64encode(file.read()).decode('utf-8')

def calculate_file_hash(file):
    """Calcola l'hash del file per evitare duplicati."""
    file.seek(0)
    return hashlib.md5(file.read()).hexdigest()

@app.route('/')
def upload_form():
    return render_template('upload.html')
@app.route('/upload', methods=['POST'])
def upload_file():
    """Gestisce l'upload del file e invia la richiesta al backend."""
    if 'file' not in request.files:
        return jsonify({"error": "Nessun file caricato"}), 400
    
    file = request.files['file']
    user_id = request.form.get('userId')  # Usa .get() per evitare KeyError
    services = request.form.getlist('services')
    file_type = request.form.get('fileType', '')  # Se mancante, restituisce stringa vuota

    if file.filename == '':
        return jsonify({"error": "File non selezionato"}), 400

    # Determiniamo se il file è un'immagine o un PDF se `fileType` non è stato impostato
    if not file_type:
        file_type = "image" if file.content_type.startswith("image") else "pdf"

    file_base64 = encode_file_to_base64(file)
    file_hash = calculate_file_hash(file)

    # Se il file è un'immagine, aggiungiamo automaticamente il servizio "context"
    if file_type == "image" and "context" not in services:
        services.append("context")

    payload = {
        "file": file_base64,
        "userId": user_id,
        "fileId": file_hash,
        "services": services
    }

    # Selezioniamo il giusto endpoint per PDF o Immagini
    upload_url = UPLOAD_IMAGE_URL if file_type == "image" else UPLOAD_PDF_URL

    response = requests.post(upload_url, json=payload)
    return response.text

@app.route('/requests', methods=['GET'])
def user_requests():
    """Recupera le richieste dell'utente e lo stato di ogni servizio."""
    user_id = request.args.get('userId')
    if not user_id:
        return jsonify({"error": "User ID richiesto"}), 400

    response = requests.get(f"{STATUS_URL}?userId={user_id}")

    if response.status_code != 200:
        return jsonify({"error": "Errore nel recupero delle richieste"}), 500

    try:
        requests_data = response.json()
        print(f"DEBUG: Risultati ricevuti -> {requests_data}")  # 👀 Stampa per debug

        for req in requests_data:
            if "serviceStatuses" not in req:
                req["serviceStatuses"] = {}

        return render_template('requests.html', requests=requests_data)

    except ValueError:
        return jsonify({"error": "Risposta non valida dal server"}), 500


@app.route('/results', methods=['GET'])
def processing_results():
    """Ottiene i risultati elaborati per una richiesta specifica."""
    request_id = request.args.get('requestId')
    if not request_id:
        return jsonify({"error": "RequestId richiesto"}), 400

    response = requests.get(f"{RESULTS_URL}?requestId={request_id}")

    if response.status_code != 200:
        return jsonify({"error": "Errore nel recupero dei risultati"}), 500

    try:
        results_data = response.json()
        print(f"DEBUG: Risultati ricevuti -> {results_data}")  # 👀 Stampa per debug

        return render_template('results.html', results=results_data)

    except ValueError:
        return jsonify({"error": "Risposta non valida dal server"}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
