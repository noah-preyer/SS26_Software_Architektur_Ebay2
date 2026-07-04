from datetime import datetime, timedelta, timezone

import jwt

from .config import Config


def generate_access_token(user_id, username, email):
    now = datetime.now(timezone.utc)
    expires_at = now + timedelta(minutes=Config.JWT_EXPIRATION_MINUTES)
    payload = {
        "sub": str(user_id),
        "username": username,
        "email": email,
        "userId": str(user_id),
        "iat": now,
        "exp": expires_at,
    }
    return jwt.encode(payload, Config.JWT_SECRET, algorithm="HS256")


def verify_token(token):
    try:
        payload = jwt.decode(token, Config.JWT_SECRET, algorithms=["HS256"])
        return payload
    except jwt.ExpiredSignatureError:
        return None
    except jwt.InvalidTokenError:
        return None
