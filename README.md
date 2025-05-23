# 📚 Library & Digital Resource App

A full‑stack solution for managing a campus library: search & reserve physical books, browse & read digital resources (PDFs), bookmark favorites, and track your reservation history—all with native Android and a JWT‑secured Flask backend.

---

## 📝 Table of Contents
1. [Features](#-features)
2. [Architecture & Tech Stack](#-architecture--tech-stack)
3. [Getting Started](#-getting-started)
   * [Prerequisites](#prerequisites)
   * [Repository Layout](#repository-layout)
   * [Server Setup (Flask)](#server-setup-flask)
   * [Mobile Setup (Android)](#mobile-setup-android)
4. [Usage Guide](#-usage-guide)
5. [API Reference](#-api-reference)
6. [CLI Commands](#-cli-commands)
7. [Contributing](#-contributing)
8. [License](#-license)

---

## 🚀 Features
| Category | Details |
|----------|---------|
| **Authentication** | Register, login, JWT‑protected routes |
| **Book Search & Reservation** | Full‑text search (title/author), real‑time availability, one‑tap reserve/cancel |
| **Digital Resources** | Browse e‑books and papers, inline PDF viewing |
| **Bookmarks** | Bookmark any book or digital resource for quick access |
| **My Reservations** | View active/past reservations, cancel active ones |
| **Overdue Alerts** | CLI‑driven notifications when due dates pass |
| **Profile Management** | View/edit name & email, delete account (archives user + cascades cancellations) |

---

## 🏗 Architecture & Tech Stack
### Client
* **Android (Kotlin)** – MVVM + LiveData, ViewBinding  
* Retrofit + Moshi for networking  
* Material 3 UI, Jetpack Navigation, Room (offline cache)

### Server
* **Python 3.9+**, **Flask**  
* SQLAlchemy (SQLite/MySQL), Flask‑JWT‑Extended, Flask‑Migrate  
* Flask‑CORS for cross‑origin support

---

## 🎬 Getting Started

### Prerequisites
| Platform | Requirements |
|----------|--------------|
| **Android** | Android Studio Flamingo+, JDK 11 |
| **Server**  | Python 3.9+, *pip*, (optional) MySQL |

### Repository Layout
```text
/
├── android-app/          # Android Studio project
├── server/               # Flask backend
│   ├── app/              # Flask app package
│   ├── migrations/       # Alembic migrations
│   ├── venv/             # (git‑ignored) Python venv
│   └── requirements.txt
├── .gitignore
└── README.md
```

### Server Setup (Flask)
```bash
# enter server folder
cd server

# create & activate venv
python -m venv venv
## Windows
venv\Scripts\activate
## macOS/Linux
source venv/bin/activate

# install deps
pip install -r requirements.txt

# copy env & configure
cp .env.example .env
# edit .env:
# DATABASE_URL=sqlite:///library.db
# JWT_SECRET_KEY=your_jwt_secret

# init DB & seed
flask db upgrade
flask seed

# run dev server
flask run --host=0.0.0.0 --port=5000
```
Base URL: `http://localhost:5000/`

### Mobile Setup (Android)
1. **Open** the `android-app/` folder in Android Studio.  
2. **Verify Base URL** in `RetrofitClient.kt` (use `http://10.0.2.2:5000/` for emulator).  
3. **Sync & build**.  
4. **Run** on emulator or device.

---

## 📱 Usage Guide
1. Register or log in.  
2. **Search Books** → enter keyword → tap **Reserve**.  
3. **Digital Resources** → browse list → open PDF inline.  
4. **Bookmarks** → tap ⭐ to save; view under **Bookmarks** tab.  
5. **My Reservations** → view/cancel.  
6. **Profile** → edit info or delete account.

---

## 🔌 API Reference
Base URL: `http://localhost:5000/`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /auth/register | – | Create an account |
| POST | /auth/login | – | Obtain a JWT |
| GET  | /auth/me | JWT | Get current user info |
| PUT  | /auth/me | JWT | Update profile |
| DELETE | /auth/delete | JWT | Delete account |
| GET | /books/search?q=… | – | Search for books |
| POST | /books/{id}/reserve | JWT | Reserve a book |
| GET | /reservations | JWT | List your reservations |
| DELETE | /reservations/{res_id} | JWT | Cancel a reservation |
| GET | /digital/ | – | List digital resources |
| GET | /digital/{id}/download | JWT | Download/view a PDF |
| GET | /bookmarks | JWT | List bookmarks |
| POST | /bookmarks | JWT | Add a bookmark (item_type, item_id) |
| DELETE | /bookmarks/{bm_id} | JWT | Remove a bookmark |

_For full schemas, see **server/app/models.py** and route files._

---

## 🛠 CLI Commands
```bash
# seed sample data
flask seed

# send overdue alerts
flask notify-overdues

# create new migration
flask db migrate -m "Add due_date"
flask db upgrade
```

---

## 🤝 Contributing
```bash
# fork & clone
git checkout -b feat/YourFeature

# commit
git commit -m "Add awesome feature"

# push & open PR
git push origin feat/YourFeature
```
Open a Pull Request and describe your changes!

---

## 📜 License
This project is licensed under the **MIT License**.
