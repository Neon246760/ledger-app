import sqlite3
from datetime import datetime

conn = sqlite3.connect("app.db")
c = conn.cursor()

# 创建用户表
c.execute("""
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
)
""")

# 创建账本表
c.execute("""
CREATE TABLE IF NOT EXISTS ledger (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT,
    FOREIGN KEY (user_id) REFERENCES users (id)
)
""")

# 创建交易表（transaction是SQLite保留关键字，需要用双引号转义）
c.execute("""
CREATE TABLE IF NOT EXISTS "transaction" (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    date TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT,
    FOREIGN KEY (user_id) REFERENCES users (id)
)
""")

# 插入测试用户（如果不存在）
c.execute("""
    INSERT OR IGNORE INTO users (username, password)
    VALUES ("WJL", "d955e5690e7e96cffea268e49c75e3f9b201fd180f2dbe10a908419d74e5fc09")
""")
c.execute("""
    INSERT OR IGNORE INTO users (username, password)
    VALUES ("ZXH", "7c20fe4f97fcf7596c6a72b032101209a81294d571cc95495294e5bc23fe0941")
""")

conn.commit()
conn.close()
