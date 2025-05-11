# app/reservations/routes.py

from flask import Blueprint, jsonify, abort
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Reservation

reservation_bp = Blueprint("reservations", __name__)

@reservation_bp.get("/")
@jwt_required()
def list_reservations():
    user_id = get_jwt_identity()
    rez = Reservation.query.filter_by(user_id=user_id).all()
    return jsonify([r.to_dict() for r in rez]), 200

@reservation_bp.delete("/<int:res_id>")
@jwt_required()
def cancel_reservation(res_id):
    user_id = get_jwt_identity()
    rez = Reservation.query.filter_by(id=res_id, user_id=user_id).first_or_404()

    # make the book available again
    book = rez.book
    if book:
        book.available = True

    db.session.delete(rez)
    db.session.commit()
    return {"msg": "Reservation canceled"}, 200
