# app/auth/routes.py

from datetime import timedelta
from flask import Blueprint, request, jsonify
from flask_jwt_extended import (
    create_access_token,
    jwt_required,
    get_jwt_identity,
    unset_jwt_cookies,
)
from ..extensions import db
from ..models import (
    User,
    DeletedUser,
    Reservation,
    Bookmark,
    Book,
)

auth_bp = Blueprint("auth", __name__)


@auth_bp.post("/register")
def register():
    data = request.get_json() or {}
    email = data.get("email")
    password = data.get("password")
    name = data.get("name")

    if not all([email, password, name]):
        return {"msg": "Missing fields"}, 400
    if User.query.filter_by(email=email.lower()).first():
        return {"msg": "User exists"}, 400

    user = User.create(email, password, name)
    db.session.add(user)
    db.session.commit()

    token = create_access_token(identity=str(user.id), expires_delta=timedelta(hours=24))
    return {
        "token": token,
        "user": {"id": user.id, "email": user.email, "name": user.name},
    }, 201


@auth_bp.post("/login")
def login():
    data = request.get_json() or {}
    email = data.get("email")
    password = data.get("password")

    user = User.query.filter_by(email=email.lower() if email else None).first()
    if not user or not user.check_password(password or ""):
        return {"msg": "Invalid credentials"}, 401

    token = create_access_token(identity=str(user.id), expires_delta=timedelta(hours=24))
    return {"token": token}, 200


@auth_bp.get("/me")
@jwt_required()
def me():
    uid_str = get_jwt_identity()
    user = User.query.get_or_404(int(uid_str))
    return {"id": user.id, "email": user.email, "name": user.name}, 200


@auth_bp.put("/me")
@jwt_required()
def update_me():
    data = request.get_json() or {}
    name = data.get("name")
    email = data.get("email")

    uid_str = get_jwt_identity()
    user = User.query.get_or_404(int(uid_str))

    if name:
        user.name = name
    if email:
        email = email.lower()
        existing = User.query.filter(User.email == email, User.id != user.id).first()
        if existing:
            return {"msg": "Email already in use"}, 400
        user.email = email

    db.session.commit()
    return {"id": user.id, "email": user.email, "name": user.name}, 200


@auth_bp.delete("/delete")
@jwt_required()
def delete_account():
    # 1) identify current user
    uid_str = get_jwt_identity()
    user = User.query.get_or_404(int(uid_str))

    # 2) archive user info
    archived = DeletedUser(
        original_user_id=user.id,
        email=user.email,
        name=user.name,
    )
    db.session.add(archived)

    # 3) for each reservation: mark book available, then delete reservation
    user_reservations = Reservation.query.filter_by(user_id=user.id).all()
    for res in user_reservations:
        book = Book.query.get(res.book_id)
        if book:
            book.available = True
        db.session.delete(res)

    # 4) delete all bookmarks
    Bookmark.query.filter_by(user_id=user.id).delete()

    # 5) delete the user record
    db.session.delete(user)
    db.session.commit()

    # 6) clear JWT cookies on client
    resp = jsonify({"msg": "Account deleted and archived"})
    unset_jwt_cookies(resp)
    return resp, 200
