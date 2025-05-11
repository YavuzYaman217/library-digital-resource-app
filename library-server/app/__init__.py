# app/__init__.py

import os
from flask import Flask
from flask_cors import CORS

from .extensions import db, migrate, jwt


def create_app():
    app = Flask(__name__)

    # ─── App config ─────────────────────────────────────────────
    app.config["SQLALCHEMY_DATABASE_URI"] = os.getenv(
        "DATABASE_URL", "sqlite:///library.db"
    )
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
    app.config["JWT_SECRET_KEY"]            = os.getenv("JWT_SECRET_KEY", "dev-secret")

    # ─── Extensions init ────────────────────────────────────────
    CORS(app)
    db.init_app(app)
    migrate.init_app(app, db)
    jwt.init_app(app)

    # ─── Blueprints ─────────────────────────────────────────────
    from .auth.routes         import auth_bp
    from .books.routes        import book_bp
    from .digital.routes      import digital_bp
    from .reservations.routes import reservation_bp
    from .bookmarks.routes    import bookmark_bp

    app.register_blueprint(auth_bp,         url_prefix="/auth")
    app.register_blueprint(book_bp,         url_prefix="/books")
    app.register_blueprint(digital_bp,      url_prefix="/digital")
    app.register_blueprint(reservation_bp,  url_prefix="/reservations")
    app.register_blueprint(bookmark_bp,     url_prefix="/bookmarks")

    # ─── CLI: overdue notifications ─────────────────────────────
    from .cli import register_commands
    register_commands(app)

    # ─── CLI: seed command ──────────────────────────────────────
    @app.cli.command("seed")
    def seed():
        """Seed the database with sample books & digital resources."""
        from .models import Book, DigitalResource

        # Clear existing data
        Book.query.delete()
        DigitalResource.query.delete()

        # Sample books
        books = [
            Book(title="Clean Code",       author="R. C. Martin", year=2008),
            Book(title="Effective Python", author="Brett Slatkin", year=2015),
            Book(title="You Don't Know JS",author="Kyle Simpson", year=2015),
            Book(title="Kotlin in Action", author="Dmitry Jemerov", year=2017),
        ]

        # Sample digital resources (make sure the PDFs exist under uploads/pdfs/)
        digital_resources = [
            DigitalResource(
                title="AI 101",
                author="Yavuz Yaman",
                type="PDF",
                file_path="uploads/pdfs/ai101.pdf"
            ),
            DigitalResource(
                title="ML Basics",
                author="Yavuz Yaman",
                type="PDF",
                file_path="uploads/pdfs/ml_basics.pdf"
            ),
        ]

        for b in books:
            db.session.add(b)
        for d in digital_resources:
            db.session.add(d)
        db.session.commit()
        click.echo("🌱 Seed data created: books and digital resources.")

    return app
