from flask import Flask, request, jsonify, send_from_directory
import os
import requests

app = Flask(__name__, static_folder="static", static_url_path="")

# адрес users-service внутри docker network (или localhost при локальной отладке)
USERS_SERVICE_URL = os.getenv("USERS_SERVICE_URL", "http://users-service:8081")

# Serve SPA
@app.get("/")
def index():
    return send_from_directory("static", "index.html")

# Generic proxy helper
def forward_to_users(path: str, method: str = "GET", headers=None, json_body=None):
    url = f"{USERS_SERVICE_URL}{path}"
    try:
        r = requests.request(method, url, headers=headers, json=json_body, timeout=10)
        # preserve status code and JSON/text body
        content_type = r.headers.get("Content-Type", "")
        if "application/json" in content_type:
            return jsonify(r.json()), r.status_code
        else:
            return (r.content, r.status_code, r.headers.items())
    except Exception as ex:
        return jsonify({"error": str(ex)}), 500

# Proxy endpoints for auth flow
@app.post("/api/auth/register")
def proxy_register():
    body = request.get_json(force=True, silent=True) or {}
    headers = {
        "Content-Type": "application/json",
        "X-Device-Id": request.headers.get("X-Device-Id", ""),
        "User-Agent": request.headers.get("User-Agent", "")
    }
    return forward_to_users("/api/auth/register", method="POST", headers=headers, json_body=body)

@app.post("/api/auth/login")
def proxy_login():
    body = request.get_json(force=True, silent=True) or {}
    headers = {
        "Content-Type": "application/json",
        "X-Device-Id": request.headers.get("X-Device-Id", ""),
        "User-Agent": request.headers.get("User-Agent", "")
    }
    return forward_to_users("/api/auth/login", method="POST", headers=headers, json_body=body)

@app.post("/api/auth/refresh")
def proxy_refresh():
    body = request.get_json(force=True, silent=True) or {}
    headers = {
        "Content-Type": "application/json",
        "X-Device-Id": request.headers.get("X-Device-Id", ""),
        "User-Agent": request.headers.get("User-Agent", "")
    }
    return forward_to_users("/api/auth/refresh", method="POST", headers=headers, json_body=body)

@app.post("/api/auth/revoke")
def proxy_revoke():
    body = request.get_json(force=True, silent=True) or {}
    headers = {
        "Content-Type": "application/json",
        "X-Device-Id": request.headers.get("X-Device-Id", ""),
        "User-Agent": request.headers.get("User-Agent", "")
    }
    return forward_to_users("/api/auth/revoke", method="POST", headers=headers, json_body=body)

@app.post("/api/auth/logout")
def proxy_logout():
    # forward Authorization header too if present
    body = request.get_json(force=True, silent=True) or {}
    headers = {
        "Content-Type": "application/json",
        "X-Device-Id": request.headers.get("X-Device-Id", ""),
        "User-Agent": request.headers.get("User-Agent", ""),
        "Authorization": request.headers.get("Authorization", "")
    }
    return forward_to_users("/api/auth/logout", method="POST", headers=headers, json_body=body)

# Proxy for protected test endpoint (example)
@app.get("/api/test/client")
def proxy_test_client():
    headers = {
        "Authorization": request.headers.get("Authorization", ""),
        "X-Device-Id": request.headers.get("X-Device-Id", ""),
        "User-Agent": request.headers.get("User-Agent", "")
    }
    return forward_to_users("/api/test/client", method="GET", headers=headers)

# Health
@app.get("/health")
def health():
    return {"status": "ok", "frontend": True, "users_url": USERS_SERVICE_URL}

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("PORT", "5000")))
