import bcrypt
from datetime import timedelta
from dotenv import load_dotenv
from flask import Flask, jsonify, request, url_for
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity, set_access_cookies
import os
from pymongo import MongoClient
from waitress import serve

load_dotenv()

app = Flask(__name__)
cors = CORS(app, supports_credentials=True)

app.config['SECRET_KEY'] = os.environ.get("SECRET_KEY")
app.config['JWT_SECRET_KEY'] = os.environ.get("JWT_SECRET_KEY")
app.config['JWT_TOKEN_LOCATION'] = [os.environ.get("JWT_TOKEN_LOCATION")]

client = MongoClient(os.environ.get("MONGO_URI"))

jwt = JWTManager(app)

@app.route('/register', methods=['GET', 'POST'])
def register():
  if request.method == 'POST':
    data = request.json
    username = data.get('username')
    password = data.get('password')
    
    if not username or not password:
        return jsonify({"error": "Username and password are required"}), 400
    
    db = client['auth_db']
    users = db['users']
    
    # Check if username already exists
    if users.find_one({"username": username}):
        return jsonify({"error": "Username already exists"}), 409

    # Hash password and insert user
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    users.insert_one({"username": username, "password": hashed_password})
    resp = jsonify({"message": "User registered successfully"})
    resp.set_cookie('username', username)
    return resp, 201
  elif request.method == 'GET':
    return jsonify({"message": "Register a new user"}), 200

@app.route('/login', methods=['GET', 'POST'])
def login():
  if request.method == 'POST':
    data = request.json
    username = data.get('username')
    password = data.get('password')
    
    if not username or not password:
        return jsonify({"error": "Username and password are required"}), 400
    
    db = client['auth_db']
    users = db['users']
    
    user = users.find_one({"username": username})
    
    if not user:
        return jsonify({"error": "Invalid username or password"}), 401
    
    if bcrypt.checkpw(password.encode('utf-8'), user['password']):
        access_token = create_access_token(identity=str(user['_id']), expires_delta=timedelta(minutes=15))
        resp = jsonify({"message": "Login Success"})
        set_access_cookies(resp, access_token)
        resp.set_cookie('username', username)
        return resp, 200
    else:
        return jsonify({"error": "Invalid username or password"}), 401
  elif request.method == 'GET':
    return jsonify({"message": "Login with your username and password"}), 200
  
@app.route('/api/validate_token', methods=['GET'])
@jwt_required()
def validate_token():
  current_user = get_jwt_identity()
  return jsonify({"valid": True, "user": current_user}), 200
  
if __name__ == '__main__':
  serve(app, host="0.0.0.0", port=3001)