import json
import logging
import signal
import smtplib
import sys
import uuid
from email.mime.text import MIMEText
from string import Template

import paho.mqtt.client as mqtt

from .config import Config
from .db import get_template, save_notification

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

EVENT_TOPICS = ["order/complete"]

TEMPLATE_CODE = "ORDER_CONFIRMATION"


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        logger.info("Connected to MQTT broker")
        for topic in EVENT_TOPICS:
            client.subscribe(topic)
            logger.info("Subscribed to %s", topic)
    else:
        logger.error("Failed to connect to MQTT broker, rc=%s", rc)


def render_template(template_code, placeholders):
    row = get_template(template_code)
    if row is None:
        logger.warning("Template %s not found", template_code)
        return None, None
    subject = Template(row["subject_template"]).safe_substitute(placeholders)
    body = Template(row["body_template"]).safe_substitute(placeholders)
    return subject, body


def send_smtp(to, subject, body):
    msg = MIMEText(body, _charset="utf-8")
    msg["Subject"] = subject
    msg["From"] = Config.SMTP_FROM
    msg["To"] = to

    try:
        with smtplib.SMTP(Config.SMTP_HOST, Config.SMTP_PORT) as server:
            server.starttls()
            if Config.SMTP_USER:
                server.login(Config.SMTP_USER, Config.SMTP_PASSWORD)
            server.sendmail(Config.SMTP_FROM, [to], msg.as_string())
        logger.info("SMTP email sent to %s", to)
    except Exception as e:
        logger.error("Failed to send SMTP email to %s: %s", to, e)
        raise


def handle_event(payload):
    notification_id = str(uuid.uuid4())

    placeholders = {k: str(v) for k, v in payload.items()}
    subject, body = render_template(TEMPLATE_CODE, placeholders)
    if subject is None:
        return

    email = payload.get("email", "")
    logger.info("Sending email to %s", email)
    logger.info("  Subject: %s", subject)

    send_smtp(email, subject, body)

    try:
        save_notification({
            "id": notification_id,
            "email": email,
            "subject": subject,
            "body": body,
            "template_code": TEMPLATE_CODE,
        })
        logger.info("Notification saved to DB with id %s", notification_id)
    except Exception as e:
        logger.error("Failed to save notification: %s", e)


def on_message(client, userdata, msg):
    try:
        payload = json.loads(msg.payload.decode())
        handle_event(payload)
    except Exception as e:
        logger.error("Failed to process message on %s: %s", msg.topic, e)


def create_client():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    return client


def main():
    client = create_client()

    def shutdown(sig, frame):
        logger.info("Shutting down...")
        client.disconnect()
        sys.exit(0)

    signal.signal(signal.SIGINT, shutdown)
    signal.signal(signal.SIGTERM, shutdown)

    try:
        logger.info("Connecting to MQTT broker at %s:%s", Config.MQTT_BROKER, Config.MQTT_PORT)
        client.connect(Config.MQTT_BROKER, Config.MQTT_PORT, 60)
        client.loop_forever()
    except Exception as e:
        logger.error("Connection error: %s", e)
        sys.exit(1)


if __name__ == "__main__":
    main()
