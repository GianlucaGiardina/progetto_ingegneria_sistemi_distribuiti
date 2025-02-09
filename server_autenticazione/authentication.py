from flask import Flask, jsonify, request
from pymongo import MongoClient
import os
from dotenv import load_dotenv
import bcrypt


load_dotenv()

app = Flask(__name__)

client = MongoClient(os.environ.get("MONGO_URI"))

@app.route('/register', methods=['GET', 'POST'])
def register():
  if request.method == 'POST':
    username = request.form.get('username')
    password = request.form.get('password')
    
    if not username or not password:
        return jsonify({"error": "Username and password are required"}), 400
    
    db = client['auth_db']
    collection = db['users']
    
    # Check if username already exists
    if collection.find_one({"username": username}):
        return jsonify({"error": "Username already exists"}), 409

    # Hash password and insert user
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    collection.insert_one({"username": username, "password": hashed_password})

    return jsonify({"message": "User registered successfully"}), 201
  elif request.method == 'GET':
    return jsonify({"message": "Register a new user"}), 200

@app.route('/login', methods=['GET', 'POST'])
def login():
  if request.method == 'POST':
    username = request.form.get('username')
    password = request.form.get('password')
    
    if not username or not password:
        return jsonify({"error": "Username and password are required"}), 400
    
    db = client['auth_db']
    collection = db['users']
    
    user = collection.find_one({"username": username})
    
    if not user:
        return jsonify({"error": "Invalid username or password"}), 401
    
    if bcrypt.checkpw(password.encode('utf-8'), user['password']):
        return jsonify({"message": "Logged in successfully"}), 200
    else:
        return jsonify({"error": "Invalid username or password"}), 401
  elif request.method == 'GET':
    return jsonify({"message": "Login with your username and password"}), 200
  
if __name__ == '__main__':
  app.run(debug=True)