from flask import Flask
from .config import Config
from .db import init_db
from .routes import register_routes


def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    register_error_handlers(app)
    register_routes(app)

    with app.app_context():
        init_db()

    return app


def register_error_handlers(app):
    from flask import jsonify

    @app.errorhandler(400)
    def bad_request(e):
        return jsonify({"message": "Bad request"}), 400

    @app.errorhandler(404)
    def not_found(e):
        return jsonify({"message": "Not found"}), 404

    @app.errorhandler(409)
    def conflict(e):
        return jsonify({"message": str(e.description)}), 409

    @app.errorhandler(401)
    def unauthorized(e):
        return jsonify({"message": str(e.description)}), 401
