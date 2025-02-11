import base64
import io
import os
import sys
import pika
from PIL import Image
from transformers import pipeline

# Parametri di connessione RabbitMQ
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "extractcontext_queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")

# Carica (la prima volta) la pipeline
captioner = pipeline("image-to-text", model="./vit-gpt2-image-captioning")


def load_image_from_base64(b64_string: str) -> Image.Image:
    """Decodifica una stringa Base64 e la trasforma in un oggetto PIL.Image."""
    try:
        image_data = base64.b64decode(b64_string)
        return Image.open(io.BytesIO(image_data)).convert("RGB")
    except Exception as e:
        print(f"Errore nella decodifica dell'immagine: {e}")
        return None


def describe_image_with_model(image_b64: str) -> str:
    """Genera una descrizione per un'immagine in formato Base64."""
    pil_img = load_image_from_base64(image_b64)
    if pil_img is None:
        return "Errore nella decodifica dell'immagine."

    result = captioner(pil_img)
    return result[0].get("generated_text", "Nessuna descrizione disponibile.") if result else "Nessuna descrizione disponibile."


def on_message_callback(ch, method, properties, body):
    """Gestisce la ricezione dei messaggi dalla coda RabbitMQ."""
    try:
        base64_str = body.decode('utf-8')
        description = describe_image_with_model(base64_str)

        # Se è una richiesta RPC, invia una risposta
        if properties.reply_to:
            ch.basic_publish(
                exchange='',
                routing_key=properties.reply_to,
                properties=pika.BasicProperties(correlation_id=properties.correlation_id),
                body=description.encode('utf-8')
            )

        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        print(f"Errore durante la gestione del messaggio: {e}", file=sys.stderr)
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


def main():
    """Avvia il consumer RabbitMQ."""
    try:
        credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
        parameters = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)

        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()

        channel.queue_declare(queue=RABBIT_QUEUE, durable=False)
        # Imposta il consumo della coda
        channel.basic_qos(prefetch_count=1)
        channel.basic_consume(queue=RABBIT_QUEUE, on_message_callback=on_message_callback)

        print(f" [*] In attesa di messaggi su '{RABBIT_QUEUE}'.")
        channel.start_consuming()


    except Exception as e:
        print(f"Errore critico: {e}", file=sys.stderr)


if __name__ == '__main__':
    main()
