import os
import pika
import logging

from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

# Configurazione Logger
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Scegli un modello T5 più potente: "google/flan-t5-large"
MODEL_NAME = "google/flan-t5-large"
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSeq2SeqLM.from_pretrained(MODEL_NAME)

# Parametri di connessione RabbitMQ
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "test_queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")


def summarize_text(text):
    """
    Funzione di summarization usando un modello T5 più potente (Flan-T5-Large).
    """
    # Prompt di summarization in stile T5
    input_text = "summarize: " + text

    inputs = tokenizer.encode(
        input_text,
        return_tensors="pt",
        max_length=512,
        truncation=True
    )

    summary_ids = model.generate(
        inputs,
        max_length=256,
        min_length=40,
        length_penalty=2.0,
        num_beams=4,
        early_stopping=True,
        no_repeat_ngram_size=3
    )

    summary = tokenizer.decode(summary_ids[0], skip_special_tokens=True)
    return summary


def callback(ch, method, properties, body):
    """
    Callback invocata quando arriva un messaggio da RabbitMQ.
    """
    message = body.decode("utf-8")
    logger.info(f"Messaggio ricevuto: {message}")

    # Esegui la summarization
    summary = summarize_text(message)
    logger.info(f"Riassunto generato: {summary}")

    # Se è una richiesta "RPC" con direct-reply-to, restituisci il risultato
    if properties.reply_to:
        # Pubblica la risposta nella coda "amq.rabbitmq.reply-to"
        ch.basic_publish(
            exchange='',
            routing_key=properties.reply_to,
            properties=pika.BasicProperties(correlation_id=properties.correlation_id),
            body=summary.encode('utf-8')
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














