import os
import pika
import logging
import spacy

# Configurazione Logger
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Carica il modello OpenNLP (spaCy con modello per NLP)
nlp = spacy.load("en_core_web_sm")

# Parametri di connessione RabbitMQ
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "nlp-queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")

def extract_named_entities(text):
    """
    Funzione che utilizza OpenNLP (tramite spaCy) per estrarre Named Entities dal testo.
    """
    doc = nlp(text)
    entities = [(ent.text, ent.label_) for ent in doc.ents]
    return entities

def callback(ch, method, properties, body):
    """
    Callback invocata quando arriva un messaggio da RabbitMQ.
    """
    message = body.decode("utf-8")
    logger.info(f"Messaggio ricevuto: {message}")

    # Esegui Named Entity Recognition (NER) con OpenNLP
    entities = extract_named_entities(message)
    logger.info(f"Entità estratte: {entities}")

    # Se è una richiesta "RPC" con direct-reply-to, restituisci il risultato
    if properties.reply_to:
        response_body = str(entities).encode('utf-8')
        ch.basic_publish(
            exchange='',
            routing_key=properties.reply_to,
            properties=pika.BasicProperties(correlation_id=properties.correlation_id),
            body=response_body
        )

    # Confermiamo il messaggio come elaborato
    ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    """
    Configura il consumer su RabbitMQ e attende nuovi messaggi.
    """
    logger.info("Connessione a RabbitMQ...")
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    parameters = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)

    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # Assicuriamoci che la coda esista
    channel.queue_declare(queue=RABBIT_QUEUE, durable=True)

    logger.info(f"Avvio consumer sulla coda: {RABBIT_QUEUE}")
    channel.basic_consume(queue=RABBIT_QUEUE, on_message_callback=callback)

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        logger.info("Arresto consumer...")
    finally:
        connection.close()

if __name__ == "__main__":
    main()
