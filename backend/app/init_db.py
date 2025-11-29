import sqlite3

conn = sqlite3.connect("app.db")
c = conn.cursor()

c.execute("""
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
)
""")
c.execute("""
    INSERT INTO users (username, password)
    VALUES ("WJL", "d955e5690e7e96cffea268e49c75e3f9b201fd180f2dbe10a908419d74e5fc09")
""")
c.execute("""
    INSERT INTO users (username, password)
    VALUES ("ZXH", "7c20fe4f97fcf7596c6a72b032101209a81294d571cc95495294e5bc23fe0941")
""")

conn.commit()
conn.close()
