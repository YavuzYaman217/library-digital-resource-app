# app/cli.py

import click
from flask import current_app
from flask.cli import with_appcontext
from datetime import datetime, timedelta

from .extensions import db
from .models     import Reservation

# Stub for your real push-notification sender
def send_push(user, title, body):
    current_app.logger.info(f"[push] to {user.email}: {title!r} / {body!r}")


@click.command("notify-overdues")
@with_appcontext
def notify_overdues():
    """
    Scan for reservations whose due_date is before now,
    and send each user a push notification.
    """
    now = datetime.utcnow()
    overdue = (
        Reservation
        .query
        .filter(Reservation.due_date < now)
        .all()
    )
    if not overdue:
        click.echo("No overdues found.")
        return

    for res in overdue:
        user  = res.user
        title = "📚 Book Overdue!"
        body  = (
            f"Your reservation for “{res.book.title}” was due "
            f"{res.due_date.strftime('%Y-%m-%d')}."
        )
        send_push(user, title, body)
        click.echo(f"→ Notified {user.email}")

    click.echo(f"Done. {len(overdue)} notification(s) sent.")


def register_commands(app):
    """Hook our CLI commands into Flask."""
    app.cli.add_command(notify_overdues)
