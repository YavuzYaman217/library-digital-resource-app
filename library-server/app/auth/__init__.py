from flask import Blueprint

# ‼️ Blueprint'i önce oluştur
auth_bp = Blueprint("auth", __name__)

# ‼️ Sonra routes'ı içe aktar
from . import routes               # noqa: E402,F401
