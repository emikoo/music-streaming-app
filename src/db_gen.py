import random
from faker import Faker
from cloudflare_utils import batch_insert, clear_all_tables
from datetime import datetime, timedelta

fake = Faker()

CONFIG = {
    'users': 15,
    'artists': 13,
    'songs': 50,
    'plays': 50,
    'playlists': 16,
    'follows': 13,
    'playlist_songs': 10
}

def generate_image_urls(base_size, count, start_random=1):
    return [f"https://picsum.photos/{base_size}/{base_size}?random={i+start_random}" for i in range(count)]

def generate_users(n):
    profile_images = generate_image_urls(200, 20, 50)
    users = []
    used_usernames, used_emails = set(), set()
    
    for _ in range(n):
        username = fake.user_name()
        while username in used_usernames:
            username = f"{fake.user_name()}_{random.randint(1000, 9999)}"
        used_usernames.add(username)
        
        email = fake.email()
        while email in used_emails:
            email = fake.email()
        used_emails.add(email)
        
        users.append((username, email, random.choice(profile_images)))
    
    return users

def generate_artists(n):
    countries = ['USA', 'Canada', 'UK', 'Australia', 'Japan', 'Germany', 'France', 'Brazil']
    artist_images = generate_image_urls(400, 8, 1)
    
    return [(fake.name(), random.choice(countries), random.choice(artist_images)) for _ in range(n)]

def generate_songs(n, artist_ids):
    song_titles = [
        "Midnight Dreams", "Electric Nights", "Sunset Boulevard", "Ocean Waves",
        "City Lights", "Dancing Stars", "Broken Hearts", "Summer Vibes",
        "Neon Glow", "Silent Whispers", "Thunder Storm", "Golden Hour",
        "Velvet Sky", "Crystal Clear", "Fire and Ice", "Moonlight Serenade",
        "Starlight Express", "Cosmic Journey", "Digital Dreams", "Retro Wave"
    ]
    
    album_covers = generate_image_urls(300, 20, 10)
    
    return [
        (random.choice(song_titles), random.choice(artist_ids), 
         random.randint(120, 300), random.choice(album_covers))
        for _ in range(n)
    ]

def generate_playlists(n, user_ids):
    playlist_names = [
        "Chill Vibes", "Workout Mix", "Road Trip", "Study Session",
        "Party Time", "Relaxing Evening", "Morning Energy", "Late Night",
        "Top Hits 2024", "Indie Favorites", "Electronic Beats", "Rock Classics",
        "Jazz Collection", "Pop Anthems", "Acoustic Sessions", "Hip Hop Essentials"
    ]
    
    playlist_covers = generate_image_urls(300, 20, 30)
    
    return [
        (random.choice(playlist_names), False, random.choice(user_ids), random.choice(playlist_covers))
        for _ in range(n)
    ]

def generate_unique_combinations(list1, list2, target_count):
    combinations = set()
    max_attempts = target_count * 3
    attempts = 0
    
    while len(combinations) < target_count and attempts < max_attempts:
        combination = (random.choice(list1), random.choice(list2))
        combinations.add(combination)
        attempts += 1
    
    return list(combinations)

def generate_plays(user_ids, song_ids, n):
    end_date = datetime.now()
    
    return [
        (random.choice(user_ids), random.choice(song_ids),
         (end_date - timedelta(days=random.randint(0, 30))).strftime('%Y-%m-%d'))
        for _ in range(n)
    ]

def populate_database():
    print("Clearing existing data...")
    clear_all_tables()
    
    print("Generating users and artists...")
    users_data = generate_users(CONFIG['users'])
    artists_data = generate_artists(CONFIG['artists'])
    
    print("Inserting users and artists...")
    user_ids = batch_insert("INSERT INTO users (username, email, profile_image) VALUES {} RETURNING id", users_data)
    artist_ids = batch_insert("INSERT INTO artists (name, country, profile_image) VALUES {} RETURNING id", artists_data)
    
    user_id_list = [user['id'] for user in user_ids] if user_ids else []
    artist_id_list = [artist['id'] for artist in artist_ids] if artist_ids else []
    
    print("Generating songs and playlists...")
    songs_data = generate_songs(CONFIG['songs'], artist_id_list)
    playlists_data = generate_playlists(CONFIG['playlists'], user_id_list)
    
    print("Inserting songs and playlists...")
    song_ids = batch_insert("INSERT INTO songs (title, artist_id, duration, album_cover) VALUES {} RETURNING id", songs_data)
    playlist_ids = batch_insert("INSERT INTO playlists (name, is_curated, created_by, cover) VALUES {} RETURNING id", playlists_data)
    
    song_id_list = [song['id'] for song in song_ids] if song_ids else []
    playlist_id_list = [playlist['id'] for playlist in playlist_ids] if playlist_ids else []
    
    print("Inserting playlist songs...")
    playlist_songs_data = generate_unique_combinations(playlist_id_list, song_id_list, CONFIG['playlist_songs'])
    batch_insert("INSERT INTO playlist_songs (playlist_id, song_id) VALUES {}", playlist_songs_data)
    
    print("Inserting plays...")
    plays_data = generate_plays(user_id_list, song_id_list, CONFIG['plays'])
    batch_insert("INSERT INTO plays (user_id, song_id, played_at) VALUES {}", plays_data)
    
    print("Inserting follows...")
    follows_data = generate_unique_combinations(user_id_list, playlist_id_list, CONFIG['follows'])
    batch_insert("INSERT INTO follows (user_id, playlist_id) VALUES {}", follows_data)
    
    print("Database populated successfully!")

if __name__ == '__main__':
    populate_database()
