import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()

print("Host:", os.getenv("DB_HOST"))
print("Password:", os.getenv("DB_PASSWORD"))

try:
    conn = psycopg2.connect(
        host=os.getenv("DB_HOST"),
        port=os.getenv("DB_PORT"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        database=os.getenv("DB_DATABASE"),
    )
    print("✅ Connected to Supabase PostgreSQL database!")
    conn.close()
except Exception as e:
    print("❌ Connection failed:")
    print(e)
    