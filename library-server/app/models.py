# app/models.py

from datetime import datetime, timedelta
from .extensions import db
from werkzeug.security import generate_password_hash, check_password_hash

class User(db.Model):
    __tablename__ = "users"
    id    = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(128), nullable=False)
    name  = db.Column(db.String(80), nullable=False)

    reservations = db.relationship(
        "Reservation",
        back_populates="user",
        cascade="all, delete-orphan",
    )

    @classmethod
    def create(cls, email, password, name):
        hashed = generate_password_hash(
            password, method="pbkdf2:sha256", salt_length=8
        )
        return cls(email=email.lower(), password_hash=hashed, name=name)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)


class Book(db.Model):
    __tablename__ = "books"
    id        = db.Column(db.Integer, primary_key=True)
    title     = db.Column(db.String(200), nullable=False)
    author    = db.Column(db.String(120), nullable=False)
    year      = db.Column(db.Integer, nullable=False)
    available = db.Column(db.Boolean, default=True, nullable=False)

    reservations = db.relationship(
        "Reservation",
        back_populates="book",
        cascade="all, delete-orphan",
    )

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "author": self.author,
            "year": self.year,
            "available": self.available,
        }


class DigitalResource(db.Model):
    __tablename__ = "digital_resources"
    id        = db.Column(db.Integer, primary_key=True)
    title     = db.Column(db.String(200), nullable=False)
    author    = db.Column(db.String(120), nullable=False)
    type      = db.Column(db.String(50), nullable=False)       # e.g. PDF, ePub
    file_path = db.Column(db.String(200), nullable=False)      # relative to project

    def to_dict(self):
        return {
            "id":        self.id,
            "title":     self.title,
            "author":    self.author,
            "type":      self.type,
            "file_path": self.file_path,
        }


class Reservation(db.Model):
    __tablename__ = "reservations"
    id         = db.Column(db.Integer, primary_key=True)
    user_id    = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    book_id    = db.Column(db.Integer, db.ForeignKey("books.id"), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    due_date   = db.Column(
        db.DateTime,
        default=lambda: datetime.utcnow() + timedelta(days=14),
        nullable=False
    )

    user = db.relationship("User", back_populates="reservations")
    book = db.relationship("Book", back_populates="reservations")

    def to_dict(self):
        return {
            "id":         self.id,
            "book":       self.book.to_dict(),
            "created_at": self.created_at.isoformat(),
            "due_date":   self.due_date.isoformat(),
        }


class Bookmark(db.Model):
    __tablename__ = "bookmarks"
    id         = db.Column(db.Integer, primary_key=True)
    user_id    = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    item_type  = db.Column(db.String(20), nullable=False)   # "book" or "digital"
    item_id    = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            "id":         self.id,
            "item_type":  self.item_type,
            "item_id":    self.item_id,
            "created_at": self.created_at.isoformat(),
        }


class DeletedUser(db.Model):
    __tablename__ = "deleted_users"
    id               = db.Column(db.Integer, primary_key=True)
    original_user_id = db.Column(db.Integer, nullable=False)
    email            = db.Column(db.String(120), nullable=False)
    name             = db.Column(db.String(80), nullable=False)
    deleted_at       = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            "id":               self.id,
            "original_user_id": self.original_user_id,
            "email":            self.email,
            "name":             self.name,
            "deleted_at":       self.deleted_at.isoformat(),
        }
