# app/books/routes.py

from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..models import Book, Reservation
from ..extensions import db

book_bp = Blueprint("books", __name__)

@book_bp.get("/search")
def search_books():
    q = request.args.get("q", "").lower()
    results = Book.query.filter(
        Book.title.ilike(f"%{q}%") | Book.author.ilike(f"%{q}%")
    ).all()
    return jsonify([b.to_dict() for b in results]), 200

@book_bp.post("/<int:book_id>/reserve")
@jwt_required()
def reserve_book(book_id):
    user_id = get_jwt_identity()
    book = Book.query.get_or_404(book_id)
    if not book.available:
        return {"msg": "Already reserved"}, 400

    # mark as reserved
    book.available = False

    # create reservation record
    rez = Reservation(user_id=user_id, book_id=book.id)
    db.session.add(rez)
    db.session.commit()

    return {"msg": "Reserved", "reservationId": rez.id}, 201
