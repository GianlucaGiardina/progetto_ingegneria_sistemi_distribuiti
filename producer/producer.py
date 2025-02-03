from flask import Flask, render_template, request, redirect, url_for, flash
import pika
import os

app = Flask(__name__)
app.secret_key = '1234'  # Necessario per flash messages

# Parametri di connessione RabbitMQ
RABBIT_HOST = os.environ.get("RABBIT_HOST", "localhost")
RABBIT_QUEUE = os.environ.get("RABBIT_QUEUE", "test_queue")
RABBIT_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "user")
RABBIT_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "pass")


def send_to_rabbitmq(message):
    """
    Invia un messaggio a RabbitMQ.
    """
    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    parameters = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()
    channel.queue_declare(queue=RABBIT_QUEUE, durable=True)
    channel.basic_publish(exchange='', routing_key=RABBIT_QUEUE, body=message)
    connection.close()


@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        text = request.form['text']
        if text:
            send_to_rabbitmq(text)
            flash('Messaggio inviato con successo a RabbitMQ!', 'success')
        else:
            flash('Il campo di testo è vuoto. Inserisci un messaggio.', 'danger')
        return redirect(url_for('index'))
    return render_template('index.html')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9091)
