from PIL import Image, ImageDraw, ImageFont, JpegImagePlugin
import textwrap


OUT = "/tmp/MediSync_CIE2_Writeup.pdf"
W, H = 1240, 1754
M = 90
BLUE = (3, 169, 244)
DARK = (17, 24, 39)
MUTED = (75, 85, 99)
LIGHT = (232, 247, 255)
LINE = (190, 215, 230)


def font(size, bold=False):
    path = "/usr/share/fonts/google-noto-vf/NotoSans[wght].ttf"
    return ImageFont.truetype(path, size=size)


F_TITLE = font(48, True)
F_H1 = font(34, True)
F_H2 = font(26, True)
F_BODY = font(23)
F_SMALL = font(18)
F_BOX = font(18)


def page():
    img = Image.new("RGB", (W, H), "white")
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W, 18], fill=BLUE)
    return img, d


def wrap(draw, text, x, y, max_width, fnt=F_BODY, fill=DARK, line_gap=8):
    words = text.split()
    lines, current = [], ""
    for word in words:
        test = (current + " " + word).strip()
        if draw.textbbox((0, 0), test, font=fnt)[2] <= max_width:
            current = test
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    for line in lines:
        draw.text((x, y), line, font=fnt, fill=fill)
        y += fnt.size + line_gap
    return y


def heading(draw, text, y):
    draw.text((M, y), text, font=F_H1, fill=BLUE)
    y += 52
    draw.line([M, y, W - M, y], fill=LINE, width=2)
    return y + 30


def bullet(draw, text, x, y, max_width):
    draw.ellipse([x, y + 10, x + 8, y + 18], fill=BLUE)
    return wrap(draw, text, x + 24, y, max_width - 24, F_BODY, DARK, 8) + 6


def section_page(title, paragraphs):
    img, d = page()
    y = heading(d, title, 80)
    for item in paragraphs:
        if isinstance(item, tuple) and item[0] == "sub":
            d.text((M, y), item[1], font=F_H2, fill=DARK)
            y += 42
        elif isinstance(item, tuple) and item[0] == "bullet":
            y = bullet(d, item[1], M, y, W - 2 * M)
        else:
            y = wrap(d, item, M, y, W - 2 * M, F_BODY, DARK, 9) + 20
    return img


def title_page():
    img, d = page()
    d.rounded_rectangle([M, 130, W - M, 500], radius=34, fill=LIGHT, outline=(185, 225, 245), width=3)
    d.text((M + 55, 205), "MediSync", font=F_TITLE, fill=BLUE)
    d.text((M + 55, 275), "CIE-2 Project Write-up", font=F_H1, fill=DARK)
    d.text((M + 55, 335), "Healthcare Appointment, Records, Chat and AI Assistance App", font=F_H2, fill=MUTED)

    y = 620
    d.text((M, y), "Submitted Content", font=F_H1, fill=DARK)
    y += 70
    for line in [
        "Title",
        "Objective",
        "Software and Hardware Requirements",
        "E-R Diagram",
        "Conclusion and Future Scope",
    ]:
        y = bullet(d, line, M, y, W - 2 * M)

    d.text((M, H - 160), "Project: MediSync", font=F_H2, fill=DARK)
    d.text((M, H - 120), "Prepared for CIE-2 documentation", font=F_BODY, fill=MUTED)
    return img


def requirements_page():
    img, d = page()
    y = heading(d, "Software and Hardware Requirements", 80)
    d.text((M, y), "Software Requirements", font=F_H2, fill=DARK)
    y += 45
    for text in [
        "Android Studio with Kotlin and Jetpack Compose for Android app development.",
        "Node.js and Express.js for backend API and WebSocket services.",
        "PostgreSQL for relational data storage and schema management.",
        "MinIO object storage for profile photos, chat files and medical reports.",
        "Firebase Cloud Messaging for push notifications.",
        "Google Maps SDK for clinic location display and navigation support.",
        "Gemini API integration for patient AI health chat.",
        "Git, Gradle, npm and Docker/PM2 for development, build and deployment.",
    ]:
        y = bullet(d, text, M + 10, y, W - 2 * M - 10)

    y += 20
    d.text((M, y), "Hardware Requirements", font=F_H2, fill=DARK)
    y += 45
    for text in [
        "Developer laptop or desktop with at least 8 GB RAM and Android Studio support.",
        "Android phone or emulator for testing patient and doctor workflows.",
        "Cloud server or local machine to run backend, database, Redis and MinIO.",
        "Stable internet connection for API calls, notifications, maps, AI chat and file access.",
    ]:
        y = bullet(d, text, M + 10, y, W - 2 * M - 10)
    return img


def er_box(d, x, y, w, h, title, fields):
    d.rounded_rectangle([x, y, x + w, y + h], radius=16, fill=(248, 252, 255), outline=BLUE, width=3)
    d.rectangle([x, y, x + w, y + 42], fill=LIGHT, outline=BLUE, width=2)
    d.text((x + 14, y + 9), title, font=F_BOX, fill=DARK)
    yy = y + 55
    for field in fields:
        d.text((x + 14, yy), field, font=F_SMALL, fill=MUTED)
        yy += 24


def line(d, x1, y1, x2, y2, label=None):
    d.line([x1, y1, x2, y2], fill=(95, 135, 160), width=3)
    if label:
        mx, my = (x1 + x2) // 2, (y1 + y2) // 2
        d.rectangle([mx - 48, my - 14, mx + 48, my + 14], fill="white")
        d.text((mx - 42, my - 12), label, font=F_SMALL, fill=MUTED)


def er_page():
    img, d = page()
    y = heading(d, "E-R Diagram", 70)
    d.text((M, y), "Major database entities and relationships used in MediSync", font=F_BODY, fill=MUTED)

    boxes = {
        "users": (430, 190, 360, 145, "USERS", ["id (PK)", "phone, name, role", "profile_photo_key"]),
        "patient": (80, 450, 330, 160, "PATIENT PROFILE", ["user_id (FK)", "personal", "medical", "lifestyle"]),
        "doctor": (830, 430, 330, 190, "DOCTOR PROFILE", ["user_id (FK)", "personal", "professional", "clinic", "availability"]),
        "slots": (790, 735, 350, 170, "APPOINTMENT_SLOTS", ["id (PK)", "doctor_id (FK)", "date, start_time", "fee, status"]),
        "appointments": (430, 720, 360, 175, "APPOINTMENTS", ["id (PK)", "patient_id (FK)", "doctor_id (FK)", "slot_id (FK)", "status, type"]),
        "chat": (95, 760, 330, 160, "CHAT_ROOMS", ["id (PK)", "patient_id (FK)", "doctor_id (FK)", "appointment_id"]),
        "messages": (80, 1045, 350, 190, "CHAT_MESSAGES", ["room_id (FK)", "sender_id (FK)", "message/file fields", "is_read, sent_at"]),
        "reports": (455, 1045, 350, 170, "MEDICAL_REPORTS", ["patient_id (FK)", "uploaded_by (FK)", "chat_room_id", "file_key"]),
        "ratings": (830, 1045, 330, 170, "DOCTOR_RATINGS", ["doctor_id (FK)", "patient_id (FK)", "appointment_id", "rating, comment"]),
    }
    for b in boxes.values():
        er_box(d, *b)

    line(d, 430, 255, 410, 525, "1:1")
    line(d, 790, 255, 830, 525, "1:1")
    line(d, 995, 620, 970, 735, "1:M")
    line(d, 830, 805, 790, 805, "1:1")
    line(d, 430, 805, 425, 840, "1:1")
    line(d, 610, 895, 610, 1045, "1:M")
    line(d, 260, 920, 255, 1045, "1:M")
    line(d, 790, 860, 915, 1045, "1:1")
    line(d, 790, 805, 1140, 525, "M:1")
    return img


def conclusion_page():
    return section_page(
        "Conclusion and Future Scope",
        [
            ("sub", "Conclusion"),
            "MediSync provides a unified healthcare platform where patients can search doctors, book online or offline appointments, communicate with doctors, store medical reports, and use AI health assistance. Doctors can manage schedules, receive appointments, chat with patients, share reports, and set clinic location. The system connects mobile UI, backend APIs, WebSockets, PostgreSQL, MinIO, Firebase notifications and maps into one practical medical workflow.",
            "The project demonstrates real-time communication, secure user authentication, appointment slot management, medical record handling, profile management, ratings, location-based clinic support and offline-friendly local caching.",
            ("sub", "Future Scope"),
            ("bullet", "Add advanced telemedicine features such as improved video call controls, call history and better picture-in-picture behavior."),
            ("bullet", "Add stronger AI guidance using verified medical guideline documents and safer emergency triage messages."),
            ("bullet", "Add payment gateway support for paid consultations and invoices."),
            ("bullet", "Add prescription generation, lab test ordering and downloadable visit summaries."),
            ("bullet", "Add analytics dashboards for doctors, including patient follow-up reminders and revenue reports."),
            ("bullet", "Improve security using production-grade secret management, encrypted storage and role-based access auditing."),
            ("bullet", "Publish production builds for Android with release signing and scalable cloud deployment."),
        ],
    )


pages = [
    title_page(),
    section_page(
        "Title",
        [
            "MediSync: A Healthcare Appointment, Medical Records, Doctor-Patient Chat and AI Health Assistance Mobile Application",
            "The project is designed as a patient and doctor platform that brings appointment booking, real-time chat, medical report sharing, clinic location, doctor ratings and AI health guidance into a single Android application.",
        ],
    ),
    section_page(
        "Objective",
        [
            "The main objective of MediSync is to simplify healthcare access by allowing patients to find suitable doctors, book appointments, communicate with doctors, view medical records and receive basic AI-assisted health guidance.",
            ("bullet", "Provide patient registration, doctor registration and secure login using OTP-based authentication."),
            ("bullet", "Allow patients to search doctors by speciality and consultation type."),
            ("bullet", "Allow doctors to manage regular and custom appointment slots with consultation fee, type and duration."),
            ("bullet", "Support doctor-patient real-time chat with image, PDF and report sharing."),
            ("bullet", "Maintain patient medical records and allow doctors to save shared files as reports."),
            ("bullet", "Show clinic location on a map and provide directions for offline appointments."),
            ("bullet", "Enable doctor rating and review after appointments."),
            ("bullet", "Provide AI health chat for general guidance, symptom discussion and report explanation."),
        ],
    ),
    requirements_page(),
    er_page(),
    conclusion_page(),
]

pages[0].save(OUT, save_all=True, append_images=pages[1:])
print(OUT)
