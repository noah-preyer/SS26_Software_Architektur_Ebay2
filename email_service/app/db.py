import psycopg2
import psycopg2.extras
from .config import Config


def get_db():
    conn = psycopg2.connect(
        host=Config.DB_HOST,
        port=Config.DB_PORT,
        dbname=Config.DB_NAME,
        user=Config.DB_USER,
        password=Config.DB_PASSWORD,
    )
    conn.autocommit = False
    return conn


def get_template(template_code):
    conn = get_db()
    try:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT subject_template, body_template FROM email_templates WHERE code = %s AND active = TRUE",
                (template_code,),
            )
            return cur.fetchone()
    finally:
        conn.close()


def save_notification(event):
    conn = get_db()
    try:
        with conn.cursor() as cur:
            cur.execute(
                """INSERT INTO email_notifications (id, recipient_email, subject, body, template_code, status, sent_at, created_at, updated_at)
                   VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW(), NOW())""",
                (event["id"], event["email"], event["subject"], event["body"],
                 event.get("template_code"), "SENT"),
            )
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
