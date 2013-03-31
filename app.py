from flask import Flask, render_template, request, redirect, url_for


app = Flask(__name__)
global _name

@app.route("/", methods = ["GET", "POST"])
def home():
    if request.method == "GET":
        return render_template("index.html")

@app.route("/others.html", methods = ["GET", "POST"])
def others():
    if request.method == "GET":
        return render_template("others.html")
    
if __name__ == "__main__":
    app.run(debug=True, host="127.0.0.1", port=5000)