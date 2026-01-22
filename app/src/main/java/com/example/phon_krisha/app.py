# app.py

from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
import os
from werkzeug.utils import secure_filename
import json

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

DATABASE_URL = "postgresql://postgres:1445@localhost:5432/hom"

def get_db_connection():
    return psycopg2.connect(DATABASE_URL, cursor_factory=RealDictCursor)

def query_db(query, args=(), one=False, commit=False):
    conn = get_db_connection()
    cur = conn.cursor()
    try:
        cur.execute(query, args)
        if commit:
            conn.commit()
        if query.strip().lower().startswith(("select", "show")):
            rows = cur.fetchall()
            return (rows[0] if rows else None) if one else rows
        return None
    except Exception as e:
        conn.rollback()
        logger.error(f"DB Error: {e}")
        raise
    finally:
        cur.close()
        conn.close()

UPLOAD_FOLDER = 'static/photos'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/upload_photo', methods=['POST'])
def upload_photo():
    if 'photo' not in request.files:
        return jsonify({"error": "No photo part"}), 400

    file = request.files['photo']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    filename = secure_filename(file.filename)
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(file_path)

    scheme = request.scheme
    host = request.host
    url = f"{scheme}://{host}/static/photos/{filename}"

    logger.info(f"Фото загружено: {filename} → {url}")
    return jsonify({"url": url})

@app.route('/static/photos/<filename>')
def serve_photo(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

# ------------------- USERS -------------------
@app.route("/register", methods=["POST"])
def register():
    data = request.get_json()
    try:
        user = query_db(
            "INSERT INTO \"user\" (fio, phone, email, password) VALUES (%s,%s,%s,%s) RETURNING id, fio, phone, email",
            [data["fio"], data["phone"], data["email"], data["password"]],
            one=True,
            commit=True
        )
        return jsonify(user or {})
    except Exception as e:
        return jsonify({"error": str(e)}), 400

@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()
    user = query_db(
        "SELECT id, fio, phone, email FROM \"user\" WHERE email=%s AND password=%s",
        [data["email"], data["password"]],
        one=True
    )
    if user:
        return jsonify(user)
    return jsonify({"error": "Invalid credentials"}), 401

@app.route("/user/<int:user_id>", methods=["GET"])
def get_user(user_id):
    user = query_db("SELECT id, fio, phone, email FROM \"user\" WHERE id=%s", [user_id], one=True)
    if user:
        return jsonify(user)
    return jsonify({"error": "User not found"}), 404

@app.route("/user/<int:user_id>", methods=["PUT"])
def update_user(user_id):
    data = request.get_json()
    params = [data["fio"], data["phone"], data["email"]]
    query = "UPDATE \"user\" SET fio=%s, phone=%s, email=%s"
    if "password" in data and data.get("password"):
        query += ", password=%s"
        params.append(data["password"])
    query += " WHERE id=%s"
    params.append(user_id)
    query_db(query, params, commit=True)
    return jsonify({"status": "ok"})

def fix_photo_urls(photos, request):
    if not photos:
        return []

    base_url = f"{request.scheme}://{request.host}/static/photos/"

    fixed = []
    for photo in photos:
        if isinstance(photo, str):
            if photo.startswith("http"):
                filename = photo.split('/static/photos/')[-1].strip('"')
            else:
                filename = photo.strip('"')
            fixed.append(base_url + filename)
    return fixed

# ------------------- ADS -------------------
@app.route("/ads", methods=["GET"])
def get_ads():
    where = []
    params = []

    # Фильтры из query params
    title = request.args.get("title")
    city = request.args.get("city")
    rooms = request.args.get("rooms", type=int)
    price_min = request.args.get("price_min", type=int)
    price_max = request.args.get("price_max", type=int)
    ad_type = request.args.get("ad_type")
    house_type = request.args.get("house_type")
    floor_min = request.args.get("floor_min", type=int)
    floor_max = request.args.get("floor_max", type=int)
    year_built_min = request.args.get("year_built_min", type=int)
    year_built_max = request.args.get("year_built_max", type=int)
    area_min = request.args.get("area_min", type=float)
    area_max = request.args.get("area_max", type=float)
    complex_name = request.args.get("complex")

    if title:
        where.append("a.title ILIKE %s")
        params.append(f"%{title}%")
    if city:
        where.append("a.city ILIKE %s")
        params.append(f"%{city}%")
    if rooms is not None:
        where.append("a.rooms = %s")
        params.append(rooms)
    if price_min is not None:
        where.append("a.price >= %s")
        params.append(price_min)
    if price_max is not None:
        where.append("a.price <= %s")
        params.append(price_max)
    if ad_type:
        where.append("a.ad_type ILIKE %s")
        params.append(f"%{ad_type}%")
    if house_type:
        where.append("a.house_type ILIKE %s")
        params.append(f"%{house_type}%")
    if floor_min is not None:
        where.append("a.floor >= %s")
        params.append(floor_min)
    if floor_max is not None:
        where.append("a.floor <= %s")
        params.append(floor_max)
    if year_built_min is not None:
        where.append("a.year_built >= %s")
        params.append(year_built_min)
    if year_built_max is not None:
        where.append("a.year_built <= %s")
        params.append(year_built_max)
    if area_min is not None:
        where.append("a.area >= %s")
        params.append(area_min)
    if area_max is not None:
        where.append("a.area <= %s")
        params.append(area_max)
    if complex_name:
        where.append("a.complex ILIKE %s")
        params.append(f"%{complex_name}%")

    query = """
        SELECT a.*,
               u.fio   as user_fio,
               u.phone as user_phone,
               a.photos
        FROM ads a
        JOIN "user" u ON a.user_id = u.id
    """
    if where:
        query += " WHERE " + " AND ".join(where)
    query += " ORDER BY a.id DESC"

    rows = query_db(query, params)

    for row in rows:
        photos_raw = row.get('photos')
        if isinstance(photos_raw, str):
            try:
                photos = json.loads(photos_raw)
            except:
                photos = []
        else:
            photos = photos_raw or []
        row['photos'] = fix_photo_urls(photos, request)

    return jsonify(rows)


@app.route("/ads/<int:ad_id>", methods=["GET"])
def get_ad(ad_id):
    row = query_db("""
                   SELECT a.*,
                          u.fio   as user_fio,
                          u.phone as user_phone,
                          a.photos
                   FROM ads a
                            JOIN "user" u ON a.user_id = u.id
                   WHERE a.id = %s
                   """, [ad_id], one=True)

    if not row:
        return jsonify({"error": "Ad not found"}), 404

    photos_raw = row.get('photos')
    if isinstance(photos_raw, str):
        try:
            photos = json.loads(photos_raw)
        except:
            photos = []
    else:
        photos = photos_raw or []
    row['photos'] = fix_photo_urls(photos, request)

    return jsonify(row)

@app.route("/ads/add", methods=["POST"])
def add_ad():
    data = request.get_json()
    photos = data.get("photos", [])
    cleaned_photos = [p.split('/static/photos/')[-1] if '/static/photos/' in p else p for p in photos]
    photos_json = json.dumps(cleaned_photos)

    query_db("""
             INSERT INTO ads (user_id, title, description, rooms, city, photos, price,
                              ad_type, house_type, floor, floors_in_house, year_built, area, complex)
             VALUES (%s, %s, %s, %s, %s, %s::jsonb, %s, %s, %s, %s, %s, %s, %s, %s)
             """, [
                 data["user_id"], data["title"], data["description"], data.get("rooms", 0),
                 data["city"], photos_json, data["price"], data["ad_type"], data["house_type"],
                 data.get("floor", 0), data.get("floors_in_house", 0), data.get("year_built", 0),
                 data.get("area", 0.0), data.get("complex")
             ], commit=True)

    return jsonify({"status": "ok"})

@app.route("/ads/update/<int:ad_id>", methods=["PUT"])
def update_ad(ad_id):
    data = request.get_json()
    photos = data.get("photos", [])
    cleaned_photos = [p.split('/static/photos/')[-1] if '/static/photos/' in p else p for p in photos]
    photos_json = json.dumps(cleaned_photos)

    query_db("""
             UPDATE ads
             SET title=%s
               , description=%s
               , rooms=%s
               , city=%s
               , photos=%s::jsonb,
            price=%s
               , ad_type=%s
               , house_type=%s
               , floor=%s
               , floors_in_house=%s
               , year_built=%s
               , area=%s
               , complex=%s
             WHERE id=%s
             """, [
                 data["title"], data["description"], data.get("rooms", 0), data["city"],
                 photos_json, data["price"], data["ad_type"], data["house_type"],
                 data.get("floor", 0), data.get("floors_in_house", 0), data.get("year_built", 0),
                 data.get("area", 0.0), data.get("complex"), ad_id
             ], commit=True)

    return jsonify({"status": "ok"})

@app.route("/ads/delete/<int:ad_id>", methods=["DELETE"])
def delete_ad(ad_id):
    query_db("DELETE FROM ads WHERE id=%s", [ad_id], commit=True)
    return jsonify({"status": "ok"})

# ------------------- FAVORITES -------------------
@app.route("/favorites/<int:user_id>", methods=["GET"])
def get_favorites(user_id):
    ads = query_db("""
                   SELECT a.*, u.fio as user_fio, u.phone as user_phone
                   FROM ads a
                            JOIN "user" u ON a.user_id = u.id
                   WHERE a.id IN (SELECT ad_id FROM favorites WHERE user_id = %s)
                   """, [user_id])
    for row in ads:
        photos_raw = row.get('photos')
        if isinstance(photos_raw, str):
            try:
                photos = json.loads(photos_raw)
            except:
                photos = []
        else:
            photos = photos_raw or []
        row['photos'] = fix_photo_urls(photos, request)
    return jsonify(ads)

@app.route("/favorites/add", methods=["POST"])
def add_favorite():
    data = request.get_json()
    query_db(
        "INSERT INTO favorites(user_id, ad_id) VALUES(%s,%s) ON CONFLICT DO NOTHING",
        [data["user_id"], data["ad_id"]], commit=True
    )
    return jsonify({"status": "ok"})

@app.route("/favorites/remove", methods=["POST"])
def remove_favorite():
    data = request.get_json()
    query_db("DELETE FROM favorites WHERE user_id=%s AND ad_id=%s",
             [data["user_id"], data["ad_id"]], commit=True)
    return jsonify({"status": "ok"})

# ------------------- MESSAGES -------------------
@app.route("/messages/<int:from_id>/<int:to_id>", methods=["GET"])
def get_messages(from_id, to_id):
    rows = query_db("""
        SELECT * FROM messages
        WHERE (from_user_id = %s AND to_user_id = %s)
           OR (from_user_id = %s AND to_user_id = %s)
        ORDER BY timestamp ASC
    """, [from_id, to_id, to_id, from_id])
    return jsonify(rows)

@app.route("/messages/send", methods=["POST"])
def send_message():
    data = request.get_json()
    query_db("""
        INSERT INTO messages (from_user_id, to_user_id, message, timestamp)
        VALUES (%s, %s, %s, NOW())
    """, [data["from_user_id"], data["to_user_id"], data["message"]], commit=True)
    return jsonify({"status": "ok"})


@app.route("/chats/<int:user_id>", methods=["GET"])
def get_chats(user_id):
    rows = query_db("""
                    SELECT DISTINCT
                    ON (partner_id)
                        partner_id,
                        (SELECT fio FROM "user" WHERE id = partner_id) AS partner_name,
                        message,
                        timestamp
                    FROM (
                        SELECT
                        CASE
                        WHEN from_user_id = %s THEN to_user_id
                        ELSE from_user_id
                        END AS partner_id, message, timestamp
                        FROM messages
                        WHERE from_user_id = %s OR to_user_id = %s
                        ) AS sub
                    ORDER BY partner_id, timestamp DESC
                    """, [user_id, user_id, user_id])

    return jsonify(rows or [])

# ------------------- RUN -------------------
if __name__ == "__main__":
    import socket
    try:
        local_ip = socket.gethostbyname(socket.gethostname())
        print("\n" + "="*60)
        print("  СЕРВЕР ЗАПУЩЕН")
        print(f"  Локально:           http://127.0.0.1:5000")
        print(f"  В локальной сети:   http://{local_ip}:5000")
        print("  Большинство роутеров используют .1 или .254 как шлюз")
        print("="*60 + "\n")
    except:
        print("Не удалось определить локальный IP. Используй ipconfig/ifconfig.")

    app.run(debug=True, host="0.0.0.0", port=5000, threaded=True)