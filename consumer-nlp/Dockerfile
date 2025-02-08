# Usa l'immagine ufficiale di Python come base
FROM python:3.9-slim

# Imposta la directory di lavoro all'interno del container
WORKDIR /app

# Copia il file requirements.txt nella directory di lavoro
COPY requirements.txt .

# Installa le dipendenze specificate in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Scarica il modello di spaCy direttamente durante la build
RUN python -m spacy download en_core_web_sm

# Copia il resto del codice dell'applicazione nella directory di lavoro
COPY . .

# Definisce la variabile d'ambiente per disabilitare il buffering dell'output di Python
ENV PYTHONUNBUFFERED=1

# Comando per eseguire il tuo script Python
CMD ["python", "consumer.py"]
