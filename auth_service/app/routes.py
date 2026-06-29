import os
import traceback
import uuid
from datetime import datetime, timezone

import bcrypt
import psycopg2.extras
from flask import jsonify, request

from .auth import generate_access_token, verify_token
from .db import get_db


def register_routes(app):
    @app.route("/register", methods=["POST"])
    def add_user():
        data = request.get_json()
        if not data:
            return jsonify({"message": "Request body is required"}), 400

        username = data.get("username", "").strip()
        email = data.get("email", "").strip()
        password = data.get("password", "")

        if not username:
            return jsonify({"message": "Username is required"}), 400
        if not email or "@" not in email:
            return jsonify({"message": "Email must be valid"}), 400
        if not password or len(password) < 8:
            return jsonify({"message": "Password must be at least 8 characters"}), 400

        conn = get_db()
        try:
            with conn.cursor() as cur:
                cur.execute("SELECT id FROM users WHERE email = %s", (email,))
                if cur.fetchone():
                    return jsonify({"message": "Email already exists"}), 409

                cur.execute("SELECT id FROM users WHERE username = %s", (username,))
                if cur.fetchone():
                    return jsonify({"message": "Username already exists"}), 409

                now = datetime.now(timezone.utc)
                password_hash = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
                user_id = str(uuid.uuid4())

                cur.execute(
                    """INSERT INTO users (id, username, email, password_hash, created_at, last_password_changed_at)
                       VALUES (%s, %s, %s, %s, %s, %s)""",
                    (user_id, username, email, password_hash, now, now),
                )
            conn.commit()
            return jsonify({"id": str(user_id), "username": username, "email": email}), 201
        except Exception as e:
            conn.rollback()
            traceback.print_exc()
            return jsonify({"message": str(e)}), 500
        finally:
            conn.close()

    @app.route("/", methods=["POST"])
    def auth_user():
        data = request.get_json()
        if not data:
            return jsonify({"message": "Email or username and password are required"}), 400

        email_or_username = data.get("emailOrUsername", "").strip()
        password = data.get("password", "")

        if not email_or_username or not password:
            return jsonify({"message": "Email or username and password are required"}), 400

        conn = get_db()
        try:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    "SELECT id, username, email, password_hash FROM users WHERE email = %s OR username = %s",
                    (email_or_username, email_or_username),
                )
                user = cur.fetchone()

            if not user:
                return jsonify({"message": "Invalid email/username or password"}), 401

            if not bcrypt.checkpw(password.encode(), user["password_hash"].encode()):
                return jsonify({"message": "Invalid email/username or password"}), 401

            token = generate_access_token(user["id"], user["username"], user["email"])
            return jsonify({"accessToken": token, "message": "Authentication successful"}), 200
        finally:
            conn.close()

    @app.route("/user/<user_id>", methods=["GET"])
    def get_user(user_id):
        conn = get_db()
        try:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                cur.execute(
                    "SELECT id, username, email, created_at FROM users WHERE id = %s",
                    (user_id,),
                )
                user = cur.fetchone()
            if not user:
                return jsonify({"message": "User not found"}), 404
            return jsonify({
                "id": str(user["id"]),
                "username": user["username"],
                "email": user["email"],
            }), 200
        finally:
            conn.close()

    @app.route("/delete/<user_id>", methods=["DELETE"])
    def delete_user(user_id):
        auth_header = request.headers.get("Authorization", "")
        internal_key = request.headers.get("X-Internal-Key", "")

        if internal_key == os.getenv("INTERNAL_API_KEY", "internal-key-dev"):
            pass
        elif auth_header.startswith("Bearer "):
            payload = verify_token(auth_header.split(" ", 1)[1])
            if payload is None:
                return jsonify({"message": "Invalid or expired token"}), 401
            if payload.get("userId") != user_id:
                return jsonify({"message": "You can only delete your own account"}), 403
        else:
            return jsonify({"message": "Missing or invalid Authorization header"}), 401

        conn = get_db()
        try:
            with conn.cursor() as cur:
                cur.execute("SELECT id FROM users WHERE id = %s", (user_id,))
                if not cur.fetchone():
                    return jsonify({"message": "User not found"}), 404

                cur.execute("DELETE FROM users WHERE id = %s", (user_id,))
            conn.commit()
            return "", 204
        except Exception as e:
            conn.rollback()
            traceback.print_exc()
            return jsonify({"message": str(e)}), 500
        finally:
            conn.close()
