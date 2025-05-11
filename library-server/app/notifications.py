# app/notifications.py
def send_push(user, title, body):
    # In prod, hook up firebase_admin.messaging.send(...)
    current_app.logger.info(f"[push] to {user.email}: {title!r} / {body!r}")
