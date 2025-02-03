# Usa l'immagine ufficiale di Python come base
FROM python:3.9-slim

# Imposta la directory di lavoro all'interno del container
WORKDIR /app

# Copia il file requirements.txt nella directory di lavoro
COPY requirements.txt .

# Installa le dipendenze specificate in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copia il resto del codice dell'applicazione nella directory di lavoro
COPY . .

# Definisci la variabile d'ambiente per disabilitare il buffering dell'output di Python
ENV PYTHONUNBUFFERED=1

# Comando per eseguire il tuo script Python
CMD ["python", "consumer.py"]
