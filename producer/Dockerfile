FROM python:3.10-slim

# Imposta la directory di lavoro all'interno del container
WORKDIR /app

# Copia i file di dipendenze e installa
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia tutti i file Python e la directory templates
COPY . .

# Comando per avviare l'app Flask
CMD ["python", "producer.py"]
