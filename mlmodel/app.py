from flask import Flask,  request
from model import calculate_risk
import logging
import json

log = logging.getLogger(__name__)
log.setLevel('INFO')

app = Flask(__name__)


@app.route("/")
def list_users():
    return {'data': 'data'}, 200  # return data and 200 OK code


@app.route("/risks")
def list_risks():
    city = request.args.get('city')
    places = json.loads(request.args.getlist('places')[0])
    print(city, places)
    log.info(city)
    log.info(places)
    return {'data': calculate_risk(city, places)}


if __name__ == "__main__":
   app.run(host='0.0.0.0', port=5000)