import psycopg2
import os
from dotenv import load_dotenv
from pathlib import Path

load_dotenv(dotenv_path=Path(__file__).resolve().parent / ".env")

def get_connection():
    return psycopg2.connect(
        host=os.getenv("DB_HOST"),
        port=os.getenv("DB_PORT"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        database=os.getenv("DB_DATABASE")
    )

def run_query(sql):
    with get_connection() as conn:
        with conn.cursor() as cur:
            cur.execute(sql)
            if cur.description:
                columns = [desc[0] for desc in cur.description]
                return [dict(zip(columns, row)) for row in cur.fetchall()]
            return []

def batch_insert(sql_prefix, values):
    if not values:
        return []

    with get_connection() as conn:
        with conn.cursor() as cur:
            placeholder = "(" + ", ".join(["%s"] * len(values[0])) + ")"
            args_str = b','.join(cur.mogrify(placeholder, row) for row in values)
            if '{}' not in sql_prefix:
                full_sql = sql_prefix + " " + args_str.decode()
            else:
                full_sql = sql_prefix.format(args_str.decode())
            cur.execute(full_sql)
            
            if cur.description:
                columns = [desc[0] for desc in cur.description]
                results = [dict(zip(columns, row)) for row in cur.fetchall()]
                conn.commit()
                return results
            
        conn.commit()
        return []




def clear_all_tables():
    with get_connection() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                DO $$
                DECLARE
                    r RECORD;
                BEGIN
                    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE;';
                    END LOOP;
                END;
                $$;
            """)
        conn.commit()
