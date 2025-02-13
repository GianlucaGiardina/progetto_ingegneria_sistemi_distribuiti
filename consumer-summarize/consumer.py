import base64
import io
import os
import sys

import pika
from PIL import Image
from transformers import pipeline

# Parametri di connessione RabbitMQ (modifica secondo la tua config o variabili d'ambiente)
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "extractcontext_queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")

# Carichiamo (una sola volta) la pipeline "image-to-text"
captioner = pipeline(
    "image-to-text",
    model="./vit-gpt2-image-captioning"  # Cartella o path locale del modello
)

def load_image_from_base64(b64_string: str) -> Image.Image:
    """
    Decodifica una stringa Base64 e la trasforma in un oggetto PIL.Image.
    """
    image_data = base64.b64decode(b64_string)
    return Image.open(io.BytesIO(image_data)).convert("RGB")

def describe_image_with_model(image_b64: str) -> str:
    """
    Riceve un'immagine in Base64, la decodifica e genera una caption
    con un modello di image captioning in locale.
    """
    pil_img = load_image_from_base64(image_b64)
    result = captioner(pil_img)  # Restituisce una lista di dict, es: [{'generated_text': '...'}]
    if len(result) > 0 and 'generated_text' in result[0]:
        return result[0]['generated_text']
    return "Nessuna descrizione disponibile."

def on_message_callback(ch, method, properties, body):
    """
    Callback invocato quando arriva un messaggio sulla coda 'extractcontext_queue'.
    """
    try:
        # 1) Estraggo il Base64 dell'immagine dal body
        base64_str = body.decode('utf-8')

        # 2) Eseguo l'estrazione del contesto con il modello
        description = describe_image_with_model(base64_str)

        # 3) Verifico se è una richiesta RPC (direct-reply-to)
        if properties.reply_to:
            # Se è una richiesta "RPC" con direct-reply-to, restituisco il risultato
            ch.basic_publish(
                exchange='',
                routing_key=properties.reply_to,
                properties=pika.BasicProperties(
                    correlation_id=properties.correlation_id
                ),
                body=description.encode('utf-8')
            )
        else:
            # Altrimenti, inoltro la descrizione nella coda 'replay'
            ch.basic_publish(
                exchange='',
                routing_key='replay',
                body=description.encode('utf-8')
            )

        # 4) Confermo l'elaborazione
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        print(f"Errore durante la descrizione dell'immagine: {e}", file=sys.stderr)
        # In caso di errore, posso scartare o re-inviare il messaggio
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

def main():
    # 1. Connessione a RabbitMQ
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    parameters = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # 2. Assicuriamoci che le code esistano
    channel.queue_declare(queue='extractcontext_queue')
    channel.queue_declare(queue='replay')  # coda per le risposte "standard"

    # 3. Consumiamo messaggi dalla coda 'extractcontext_queue'
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(
        queue='extractcontext_queue',
        on_message_callback=on_message_callback
    )

    print(" [*] In attesa di messaggi su 'extractcontext_queue'. Per interrompere: CTRL+C")
    channel.start_consuming()

if __name__ == '__main__':
    main()
