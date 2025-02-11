# Usa un'immagine leggera di Python come base
FROM python:3.9-slim

# Installa dipendenze di sistema
RUN apt-get update && apt-get install -y \
    git \
    git-lfs \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Installa Git LFS e clona il modello Hugging Face
RUN git lfs install && \
    git clone https://huggingface.co/nlpconnect/vit-gpt2-image-captioning /app/vit-gpt2-image-captioning

# Imposta la directory di lavoro
WORKDIR /app

# Copia e installa i requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia tutto il resto del codice dell'applicazione
COPY . .

# Definisci la variabile d'ambiente per evitare il buffering dell'output di Python
ENV PYTHONUNBUFFERED=1

# Comando per avviare il consumer
CMD ["python", "consumer.py"]
