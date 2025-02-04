import os
import uuid
import pika

from flask import Flask, render_template, request, redirect, url_for, flash

app = Flask(__name__)
app.secret_key = '1234'  # Necessario per i flash messages

# Parametri di connessione RabbitMQ
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "test_queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")

def call_summarization_rpc(message):
    """
    Invia il testo da riassumere (message) al consumer tramite RabbitMQ
    e attende la risposta (il riassunto) usando la direct-reply-to queue.
    """
    # 1) Connessione
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    parameters = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # 2) Generiamo un correlation_id univoco per tracciare la risposta
    correlation_id = str(uuid.uuid4())

    # Variabile dove salvare il risultato della summarization
    response = None

    # Callback locale chiamata quando arriva un messaggio sulla coda di reply_to
    def on_response(ch, method, props, body):
        nonlocal response
        # Verifichiamo che la risposta abbia lo stesso correlation_id
        if props.correlation_id == correlation_id:
            response = body.decode('utf-8')

    # 3) Consumiamo i messaggi sulla "pseudo-coda" amq.rabbitmq.reply-to
    # con auto_ack=True (qui non serve ack su risposte).
    channel.basic_consume(
        queue='amq.rabbitmq.reply-to',
        on_message_callback=on_response,
        auto_ack=True
    )

    # 4) Pubblica il messaggio con la proprietà reply_to e correlation_id
    channel.basic_publish(
        exchange='',
        routing_key=RABBIT_QUEUE,
        properties=pika.BasicProperties(
            reply_to='amq.rabbitmq.reply-to',
            correlation_id=correlation_id,
        ),
        body=message.encode('utf-8')
    )

    # 5) Aspetta finché non arriva la risposta con lo stesso correlation_id
    while response is None:
        connection.process_data_events()  # Lascia elaborare gli eventi di rete

    # Chiudiamo la connessione
    connection.close()
    return response


@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        text = request.form['text']
        if text:
            try:
                # Chiamata sincrona RPC
                summary = call_summarization_rpc(text)
                flash(f"Riassunto ricevuto:\n{summary}", 'success')
            except Exception as e:
                flash(f"Errore durante l'invio/ricezione da RabbitMQ: {e}", 'danger')
        else:
            flash('Il campo di testo è vuoto. Inserisci un messaggio.', 'danger')
        return redirect(url_for('index'))
    return render_template('index.html')


if __name__ == '__main__':
    # Avvia la Flask app su 0.0.0.0:9091
    app.run(host='0.0.0.0', port=9091)
