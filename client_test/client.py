import base64
import hashlib
import requests
from flask import Flask, render_template, request, jsonify

app = Flask(__name__)
UPLOAD_URL = "http://localhost:8080/api/process"  # URL del server Spring Boot

def encode_file_to_base64(file):
    return base64.b64encode(file.read()).decode('utf-8')

def calculate_file_hash(file):
    file.seek(0)
    return hashlib.md5(file.read()).hexdigest()

@app.route('/')
def upload_form():
    return render_template('upload.html')

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify({"error": "Nessun file caricato"}), 400
    
    file = request.files['file']
    user_id = request.form['userId']
    services = request.form.getlist('services')
    
    if file.filename == '':
        return jsonify({"error": "File non selezionato"}), 400
    
    file_base64 = encode_file_to_base64(file)
    file_hash = calculate_file_hash(file)
    
    payload = {
        "file": file_base64,
        "userId": user_id,
        "fileId": file_hash,
        "services": services
    }
    
    response = requests.post(UPLOAD_URL, json=payload)
    return response.text

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
