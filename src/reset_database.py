import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()

def reset_database():
    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD"),
            database=os.getenv("DB_DATABASE"),
        )
        
        with conn.cursor() as cur:
            drop_tables = [
                "DROP TABLE IF EXISTS follows CASCADE;",
                "DROP TABLE IF EXISTS plays CASCADE;", 
                "DROP TABLE IF EXISTS playlist_songs CASCADE;",
                "DROP TABLE IF EXISTS playlists CASCADE;",
                "DROP TABLE IF EXISTS songs CASCADE;",
                "DROP TABLE IF EXISTS artists CASCADE;",
                "DROP TABLE IF EXISTS users CASCADE;"
            ]
            
            for drop_sql in drop_tables:
                cur.execute(drop_sql)
            
            print("🗑️ Dropped existing tables")
            
            with open('../schema.sql', 'r') as file:
                schema_sql = file.read()
            
            cur.execute(schema_sql)
            conn.commit()
            print("✅ Schema applied successfully!")
            
    except Exception as e:
        print(f"❌ Error resetting database: {e}")
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    reset_database()