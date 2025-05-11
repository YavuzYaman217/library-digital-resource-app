import os
from pathlib import Path
from flask import Blueprint, jsonify, send_file, abort, current_app
from flask_jwt_extended import jwt_required
from ..models import DigitalResource
from ..extensions import db

digital_bp = Blueprint("digital", __name__)

# Where your PDFs live (project root/uploads/pdfs)
UPLOAD_DIR = Path(os.getcwd()) / "uploads" / "pdfs"
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


def sync_resources():
    """
    Scan uploads/pdfs for new files, and insert into DB if missing.
    Logs each new file for debugging.
    """
    added = 0
    for file_path in UPLOAD_DIR.glob("*.*"):
        # build relative path like "uploads/pdfs/filename.pdf"
        rel = file_path.relative_to(Path(os.getcwd())).as_posix()

        # skip if already in DB
        if DigitalResource.query.filter_by(file_path=rel).first():
            continue

        current_app.logger.debug(f"Sync: Adding new resource for file {rel}")

        # derive metadata
        stem = file_path.stem                   # "my_doc"
        ext  = file_path.suffix.lstrip(".").upper()  # "PDF"
        dr = DigitalResource(
            title=stem.replace("_", " ").title(),
            author="Unknown",
            type=ext,
            file_path=rel
        )
        db.session.add(dr)
        added += 1

    if added:
        db.session.commit()
        current_app.logger.info(f"Sync: Added {added} new digital resource(s)")
    else:
        current_app.logger.debug("Sync: No new files found")


@digital_bp.route("", methods=["GET"])
@digital_bp.route("/", methods=["GET"])
def list_resources():
    # sync on every listing
    sync_resources()

    resources = DigitalResource.query.all()
    return jsonify([r.to_dict() for r in resources]), 200


@digital_bp.get("/<int:res_id>/download")
@jwt_required()
def download_resource(res_id):
    res = DigitalResource.query.get_or_404(res_id)
    path = Path(os.getcwd()) / res.file_path

    current_app.logger.debug(f"Download: Serving file at {path}")
    if not path.is_file():
        abort(404, description="File not found on server")

    return send_file(str(path), as_attachment=False)
