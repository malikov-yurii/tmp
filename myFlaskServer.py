from flask import Flask, request, jsonify
import os

app = Flask(__name__)
DIR = 'myvideos'
if not os.path.exists(DIR):
    os.makedirs(DIR)

@app.route('/persistVideo', methods=['POST'])
def upload_video():
    file = request.files['file']
    filepath = os.path.join(DIR, file.filename)
    file.save(filepath)
    return jsonify({'message': 'UPLOAD SUCCESS!!!', 'filepath': filepath}), 201

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9999)