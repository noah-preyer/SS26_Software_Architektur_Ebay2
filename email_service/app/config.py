import os


class Config:
    MQTT_BROKER = os.getenv("MQTT_BROKER", "mqtt-broker")
    MQTT_PORT = int(os.getenv("MQTT_PORT", "1883"))

    DB_HOST = os.getenv("DB_HOST", "email-db")
    DB_PORT = os.getenv("DB_PORT", "5432")
    DB_NAME = os.getenv("DB_NAME", "email_service")
    DB_USER = os.getenv("DB_USER", "email_user")
    DB_PASSWORD = os.getenv("DB_PASSWORD", "app_password")

    SMTP_HOST = os.getenv("SMTP_HOST", "smtp.gmail.com")
    SMTP_PORT = int(os.getenv("SMTP_PORT", "587"))
    SMTP_USER = os.getenv("SMTP_USER", "")
    SMTP_PASSWORD = os.getenv("SMTP_PASSWORD", "")
    SMTP_FROM = os.getenv("SMTP_FROM", "")
