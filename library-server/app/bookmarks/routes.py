# app/bookmarks/routes.py

import os
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Bookmark, Book, DigitalResource

bookmark_bp = Blueprint("bookmarks", __name__)

@bookmark_bp.get("/")
@jwt_required()
def list_bookmarks():
    user_id = get_jwt_identity()
    bookmarks = Bookmark.query.filter_by(user_id=user_id).all()

    result = []
    for bm in bookmarks:
        # load the actual item
        if bm.item_type == "book":
            obj = Book.query.get_or_404(bm.item_id)
            item_dict = obj.to_dict()
        else:
            obj = DigitalResource.query.get_or_404(bm.item_id)
            item_dict = {
                "id": obj.id,
                "title": obj.title,
                "author": obj.author,
                "type": obj.type
            }

        result.append({
            "id":         bm.id,
            "item_type":  bm.item_type,
            "item_id":    bm.item_id,
            "created_at": bm.created_at.isoformat(),
            "item":       item_dict
        })

    return jsonify(result), 200

@bookmark_bp.post("/")
@jwt_required()
def add_bookmark():
    data      = request.get_json() or {}
    user_id   = get_jwt_identity()
    item_type = data.get("item_type")
    item_id   = data.get("item_id")

    if item_type not in ("book", "digital") or not isinstance(item_id, int):
        return {"msg": "Bad payload"}, 400

    # ensure the referenced object exists
    if item_type == "book":
        Book.query.get_or_404(item_id)
    else:
        DigitalResource.query.get_or_404(item_id)

    # prevent duplicates
    existing = Bookmark.query.filter_by(
        user_id=user_id,
        item_type=item_type,
        item_id=item_id
    ).first()
    if existing:
        return {"msg": "Already bookmarked"}, 400

    bm = Bookmark(user_id=user_id, item_type=item_type, item_id=item_id)
    db.session.add(bm)
    db.session.commit()

    # build same response shape as list
    if bm.item_type == "book":
        obj = Book.query.get(bm.item_id)
        item_dict = obj.to_dict()
    else:
        obj = DigitalResource.query.get(bm.item_id)
        item_dict = {
            "id": obj.id,
            "title": obj.title,
            "author": obj.author,
            "type": obj.type
        }

    return jsonify({
        "id":         bm.id,
        "item_type":  bm.item_type,
        "item_id":    bm.item_id,
        "created_at": bm.created_at.isoformat(),
        "item":       item_dict
    }), 201

@bookmark_bp.delete("/<int:bm_id>")
@jwt_required()
def delete_bookmark(bm_id):
    user_id = get_jwt_identity()
    bm = Bookmark.query.filter_by(id=bm_id, user_id=user_id).first_or_404()
    db.session.delete(bm)
    db.session.commit()
    return {"msg": "Deleted"}, 200
