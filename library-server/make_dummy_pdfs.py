from reportlab.pdfgen import canvas
import os

os.makedirs("uploads/pdfs", exist_ok=True)

for name, text in [("ai101.pdf", "AI 101 Dummy PDF"), ("ml_basics.pdf", "ML Basics Dummy PDF")]:
    c = canvas.Canvas(f"uploads/pdfs/{name}")
    c.drawString(100, 750, text)
    c.save()
    print(f"Created uploads/pdfs/{name}")
