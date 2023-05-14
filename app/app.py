from flask import Flask


def create_app():
    app = Flask(__name__)

    @app.route("/")
    def hello_world():
        return "Hello, World!"

    return app


if __name__ == "__main__":
    app = create_app()
    app.run(threaded=True,host='0.0.0.0',port=5000)

