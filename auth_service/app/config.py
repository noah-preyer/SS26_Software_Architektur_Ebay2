import os


class Config:
    DB_HOST = os.getenv("DB_HOST", "auth-db")
    DB_PORT = os.getenv("DB_PORT", "5432")
    DB_NAME = os.getenv("DB_NAME", "auth_service")
    DB_USER = os.getenv("DB_USER", "auth_user")
    DB_PASSWORD = os.getenv("DB_PASSWORD", "app_password")
    JWT_SECRET = os.getenv("JWT_SECRET", "ss26-software-architecture-group-5")
    JWT_EXPIRATION_MINUTES = int(os.getenv("JWT_EXPIRATION_MINUTES", "60"))
