from flask import Flask, request, jsonify, send_from_directory
import psycopg2
import os
from dotenv import load_dotenv
from flask_cors import CORS
from functools import lru_cache

load_dotenv()
app = Flask(__name__)
CORS(app, supports_credentials=True)

def get_db_connection():
    return psycopg2.connect(
        host=os.getenv("DB_HOST"),
        database=os.getenv("DB_DATABASE"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        port=os.getenv("DB_PORT")
    )

@app.route('/songs')
def get_songs():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 20, type=int)
    search = request.args.get('search', '', type=str)
    offset = (page - 1) * per_page
    
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        base_query = """
            SELECT s.id, s.title, s.artist_id, s.duration, s.album_cover, a.name as artist_name 
            FROM songs s 
            JOIN artists a ON s.artist_id = a.id 
        """
        
        if search:
            base_query += """
                WHERE LOWER(s.title) LIKE LOWER(%s) OR LOWER(a.name) LIKE LOWER(%s)
            """
            search_param = f"%{search}%"
            params = (search_param, search_param, per_page, offset)
        else:
            params = (per_page, offset)
            
        base_query += " ORDER BY s.title LIMIT %s OFFSET %s"
        
        cur.execute(base_query, params)
        
        rows = cur.fetchall()
        songs = []
        for row in rows:
            songs.append({
                'id': row[0],
                'title': row[1],
                'artist_id': row[2],
                'duration': row[3],
                'album_cover': row[4],
                'artist_name': row[5]
            })
        
        return jsonify(songs)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/top-songs', methods=['GET'])
def get_top_songs():
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            cur.execute("""
                SELECT s.id, s.title, a.name as artist, COUNT(*) as play_count, s.album_cover
                FROM plays p
                JOIN songs s ON p.song_id = s.id
                LEFT JOIN artists a ON s.artist_id = a.id
                WHERE p.played_at >= CURRENT_DATE - INTERVAL '7 days'
                GROUP BY s.id, s.title, a.name, s.album_cover
                ORDER BY play_count DESC
                LIMIT 10
            """)
            
            columns = [desc[0] for desc in cur.description]
            results = [dict(zip(columns, row)) for row in cur.fetchall()]
            
        return jsonify(results)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        if conn:
            conn.close()

@app.route('/top-users', methods=['GET'])
def get_top_users():
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            cur.execute("""
                SELECT u.id, u.username, u.email, u.profile_image, u.created_at,
                       COALESCE(SUM(s.duration), 0) as total_playtime
                FROM users u
                LEFT JOIN plays p ON u.id = p.user_id
                LEFT JOIN songs s ON p.song_id = s.id
                GROUP BY u.id, u.username, u.email, u.profile_image, u.created_at
                ORDER BY total_playtime DESC
                LIMIT 3
            """)
            
            columns = [desc[0] for desc in cur.description]
            results = [dict(zip(columns, row)) for row in cur.fetchall()]
            
            return jsonify(results)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        conn.close()

@app.route('/user-playtime', methods=['GET'])
def get_user_playtime():
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            cur.execute("""
                SELECT u.id, u.username, u.email, u.profile_image, u.created_at,
                       COALESCE(SUM(s.duration), 0) as total_playtime
                FROM users u
                LEFT JOIN plays p ON u.id = p.user_id
                LEFT JOIN songs s ON p.song_id = s.id
                GROUP BY u.id, u.username, u.email, u.profile_image, u.created_at
                ORDER BY total_playtime DESC
            """)
            
            columns = [desc[0] for desc in cur.description]
            results = []
            for row in cur.fetchall():
                user_data = dict(zip(columns, row))
                results.append({
                    'user': {
                        'id': user_data['id'],
                        'username': user_data['username'],
                        'email': user_data['email'],
                        'profile_image': user_data['profile_image'],
                        'created_at': user_data['created_at'].isoformat() if user_data['created_at'] else None
                    },
                    'total_playtime': user_data['total_playtime']
                })
            
            return jsonify(results)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        conn.close()

@app.route('/top-playlists', methods=['GET'])
def get_top_playlists():
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("""
            SELECT p.id, p.name, p.cover, COALESCE(play_counts.total_plays, 0) as play_count
            FROM playlists p
            LEFT JOIN (
                SELECT ps.playlist_id, COUNT(pl.id) as total_plays
                FROM playlist_songs ps
                LEFT JOIN plays pl ON ps.song_id = pl.song_id
                GROUP BY ps.playlist_id
            ) play_counts ON p.id = play_counts.playlist_id
            ORDER BY play_count DESC, p.created_at DESC
            LIMIT 5
        """)
        rows = cur.fetchall()
        
        return jsonify([{
            'id': row[0],
            'name': row[1],
            'cover': row[2],
            'play_count': row[3]
        } for row in rows])
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/playlists', methods=['GET'])
def get_playlists():
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("""
            SELECT p.id, p.name, p.is_curated, p.created_by, p.created_at, p.cover, 
                   u.username as creator_username, COALESCE(play_counts.total_plays, 0) as play_count
            FROM playlists p
            LEFT JOIN users u ON p.created_by = u.id
            LEFT JOIN (
                SELECT ps.playlist_id, COUNT(pl.id) as total_plays
                FROM playlist_songs ps
                LEFT JOIN plays pl ON ps.song_id = pl.song_id
                GROUP BY ps.playlist_id
            ) play_counts ON p.id = play_counts.playlist_id
            ORDER BY p.created_at DESC
        """)
        rows = cur.fetchall()
        
        return jsonify([{
            'id': row[0],
            'name': row[1],
            'is_curated': row[2],
            'created_by': row[3],
            'created_at': row[4].isoformat() if row[4] else None,
            'cover': row[5],
            'creator_username': row[6],
            'play_count': row[7]
        } for row in rows])
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/playlists', methods=['POST'])
def create_playlist():
    data = request.get_json()
    
    if not data or 'name' not in data:
        return jsonify({'error': 'Missing required field: name'}), 400
    
    name = data['name'].strip()
    description = data.get('description', '').strip()
    cover = data.get('cover', None)
    
    if not name:
        return jsonify({'error': 'Playlist name cannot be empty'}), 400
    
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        
        cur.execute("SELECT id, username FROM users ORDER BY RANDOM() LIMIT 1")
        user_row = cur.fetchone()
        
        if not user_row:
            return jsonify({'error': 'No users found in database'}), 500
        
        random_user_id, random_username = user_row
        
        cur.execute("""
            INSERT INTO playlists (name, created_by, is_curated, cover, description) 
            VALUES (%s, %s, %s, %s, %s) 
            RETURNING id, name, is_curated, created_by, created_at, cover, description
        """, (name, random_user_id, False, cover, description))
        
        playlist_row = cur.fetchone()
        conn.commit()
        
        return jsonify({
            'id': playlist_row[0],
            'name': playlist_row[1],
            'is_curated': playlist_row[2],
            'created_by': playlist_row[3],
            'created_at': playlist_row[4].isoformat() if playlist_row[4] else None,
            'cover': playlist_row[5],
            'creator_username': random_username,
            'play_count': 0
        }), 201
        
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/plays', methods=['POST'])
def add_play():
    data = request.get_json()
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("INSERT INTO plays (user_id, song_id) VALUES (%s, %s) RETURNING id", 
                   (data['user_id'], data['song_id']))
        play = cur.fetchone()
        conn.commit()
        return jsonify({'id': play[0]}), 201
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cur.close()
        conn.close()

@app.route('/follows', methods=['POST'])
def follow_playlist():
    data = request.get_json()
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("INSERT INTO follows (user_id, playlist_id) VALUES (%s, %s)", 
                   (data['user_id'], data['playlist_id']))
        conn.commit()
        return jsonify({'message': 'Playlist followed'}), 201
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cur.close()
        conn.close()

@app.route('/users', methods=['GET'])
def handle_users():
    if request.method == 'GET':
        conn = get_db_connection()
        try:
            cur = conn.cursor()
            cur.execute("SELECT id, username, email, profile_image, created_at FROM users")
            rows = cur.fetchall()
            users = []
            for row in rows:
                users.append({
                    'id': row[0],
                    'username': row[1],
                    'email': row[2],
                    'profile_image': row[3],
                    'created_at': row[4].isoformat() if row[4] else None
                })
            return jsonify(users)
        except Exception as e:
            return jsonify({'error': str(e)}), 500
        finally:
            cur.close()
            conn.close()

@app.route('/songs/<int:song_id>', methods=['GET'])
def get_song_details(song_id):
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("""
            SELECT s.id, s.title, s.artist_id, s.duration, s.album_cover, 
                   a.name as artist_name, a.country, a.profile_image as artist_image
            FROM songs s 
            JOIN artists a ON s.artist_id = a.id 
            WHERE s.id = %s
        """, (song_id,))
        
        song_row = cur.fetchone()
        if not song_row:
            return jsonify({'error': 'Song not found'}), 404
            
        cur.execute("""
            SELECT COUNT(*) as play_count
            FROM plays p
            WHERE p.song_id = %s
        """, (song_id,))
        
        play_count = cur.fetchone()[0]
        
        song_details = {
            'id': song_row[0],
            'title': song_row[1],
            'artist_id': song_row[2],
            'duration': song_row[3],
            'album_cover': song_row[4],
            'artist_name': song_row[5],
            'artist_country': song_row[6],
            'artist_image': song_row[7],
            'play_count': play_count
        }
        
        return jsonify(song_details)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/playlists/<int:playlist_id>', methods=['GET'])
def get_playlist_details(playlist_id):
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("""
            SELECT p.id, p.name, p.is_curated, p.created_by, p.created_at, p.cover,
                   u.username as creator_username, u.profile_image as creator_image
            FROM playlists p
            LEFT JOIN users u ON p.created_by = u.id
            WHERE p.id = %s
        """, (playlist_id,))
        
        playlist_row = cur.fetchone()
        if not playlist_row:
            return jsonify({'error': 'Playlist not found'}), 404
            
        cur.execute("""
            SELECT s.id, s.title, s.duration, s.album_cover, a.name as artist_name
            FROM playlist_songs ps
            JOIN songs s ON ps.song_id = s.id
            JOIN artists a ON s.artist_id = a.id
            WHERE ps.playlist_id = %s
            ORDER BY s.title
        """, (playlist_id,))
        
        songs = []
        for song_row in cur.fetchall():
            songs.append({
                'id': song_row[0],
                'title': song_row[1],
                'duration': song_row[2],
                'album_cover': song_row[3],
                'artist_name': song_row[4]
            })
            
        cur.execute("""
            SELECT COUNT(pl.id) as total_plays
            FROM playlist_songs ps
            LEFT JOIN plays pl ON ps.song_id = pl.song_id
            WHERE ps.playlist_id = %s
        """, (playlist_id,))
        
        total_plays = cur.fetchone()[0] or 0
        
        playlist_details = {
            'id': playlist_row[0],
            'name': playlist_row[1],
            'is_curated': playlist_row[2],
            'created_by': playlist_row[3],
            'created_at': playlist_row[4].isoformat() if playlist_row[4] else None,
            'cover': playlist_row[5],
            'creator_username': playlist_row[6],
            'creator_image': playlist_row[7],
            'songs': songs,
            'song_count': len(songs),
            'total_plays': total_plays
        }
        
        return jsonify(playlist_details)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

@app.route('/users/<int:user_id>', methods=['GET'])
def get_user_details(user_id):
    conn = get_db_connection()
    try:
        cur = conn.cursor()
        cur.execute("""
            SELECT id, username, email, profile_image, created_at
            FROM users
            WHERE id = %s
        """, (user_id,))
        
        user_row = cur.fetchone()
        if not user_row:
            return jsonify({'error': 'User not found'}), 404
            
        cur.execute("""
            SELECT id, name, cover, created_at
            FROM playlists
            WHERE created_by = %s
            ORDER BY created_at DESC
        """, (user_id,))
        
        playlists = []
        for playlist_row in cur.fetchall():
            playlists.append({
                'id': playlist_row[0],
                'name': playlist_row[1],
                'cover': playlist_row[2],
                'created_at': playlist_row[3].isoformat() if playlist_row[3] else None
            })
            
        cur.execute("""
            SELECT COALESCE(SUM(s.duration), 0) as total_playtime
            FROM plays p
            JOIN songs s ON p.song_id = s.id
            WHERE p.user_id = %s
        """, (user_id,))
        
        total_playtime = cur.fetchone()[0]
        
        cur.execute("""
            SELECT s.id, s.title, a.name as artist_name, COUNT(*) as play_count
            FROM plays p
            JOIN songs s ON p.song_id = s.id
            JOIN artists a ON s.artist_id = a.id
            WHERE p.user_id = %s
            GROUP BY s.id, s.title, a.name
            ORDER BY play_count DESC
            LIMIT 5
        """, (user_id,))
        
        top_songs = []
        for song_row in cur.fetchall():
            top_songs.append({
                'id': song_row[0],
                'title': song_row[1],
                'artist_name': song_row[2],
                'play_count': song_row[3]
            })
        
        user_details = {
            'id': user_row[0],
            'username': user_row[1],
            'email': user_row[2],
            'profile_image': user_row[3],
            'created_at': user_row[4].isoformat() if user_row[4] else None,
            'playlists': playlists,
            'playlist_count': len(playlists),
            'total_playtime': total_playtime,
            'top_songs': top_songs
        }
        
        return jsonify(user_details)
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cur.close()
        conn.close()

if __name__ == '__main__':
    app.run(debug=True)
