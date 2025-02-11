import os
import uuid
import pika
import base64

from flask import Flask, render_template, request, redirect, url_for, flash

app = Flask(__name__)
app.secret_key = '1234'  # Necessario per i flash messages

# Parametri di connessione RabbitMQ
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "extractcontext_queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")
def send_image_and_get_reply(image_base64: str) -> str:
    """
    Invia l'immagine in Base64 al consumer tramite RabbitMQ in modalità RPC (reply-to)
    e attende la risposta dal consumer.
    """
    try:
        credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
        parameters = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()

        # Dichiarazione coda (crea la coda se non esiste)
        channel.queue_declare(queue=RABBIT_QUEUE, durable=False)

        correlation_id = str(uuid.uuid4())
        result_container = {"result": None}

        def on_response(ch, method, props, body):
            if props.correlation_id == correlation_id:
                result_container["result"] = body.decode('utf-8')
                ch.stop_consuming()

        # Sottoscriviamo la callback su 'amq.rabbitmq.reply-to'
        channel.basic_consume(
            queue='amq.rabbitmq.reply-to',
            on_message_callback=on_response,
            auto_ack=True
        )

        channel.basic_publish(
            exchange='',
            routing_key=RABBIT_QUEUE,
            properties=pika.BasicProperties(
                reply_to='amq.rabbitmq.reply-to',
                correlation_id=correlation_id
            ),
            body=image_base64.encode('utf-8')
        )

        channel.start_consuming()
        connection.close()

        return result_container["result"]

    except Exception as e:
        print(f"Errore nell'invio/attesa risposta: {e}")
        return None



@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        if 'image' not in request.files:
            flash('Nessun file selezionato.', 'danger')
            return redirect(url_for('index'))

        file = request.files['image']
        if file.filename == '':
            flash('Seleziona un file prima di inviare.', 'danger')
            return redirect(url_for('index'))

        try:
            # 1) Leggi l'immagine e converti in Base64
            image_base64 = base64.b64encode(file.read()).decode('utf-8')

            # 2) Invia l'immagine a RabbitMQ e attendi la risposta
            result = send_image_and_get_reply(image_base64)

            if result is not None:
                flash('Immagine inviata con successo a RabbitMQ.', 'success')
                flash(f"Risposta ricevuta dal consumer: {result}", 'info')
            else:
                flash('Errore durante l\'invio o la ricezione della risposta.', 'danger')

        except Exception as e:
            flash(f"Errore durante l'elaborazione dell'immagine: {e}", 'danger')

        return redirect(url_for('index'))

    return render_template('index.html')


if __name__ == '__main__':
    # Avvia la Flask app su 0.0.0.0:9091
    app.run(host='0.0.0.0', port=9091)
