PRAGMA foreign_keys = ON;
CREATE TABLE IF NOT EXISTS trains (
id INTEGER PRIMARY KEY AUTOINCREMENT,
capacity INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS stations (
id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT NOT NULL,
location TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS routes (
id INTEGER PRIMARY KEY AUTOINCREMENT
);
CREATE TABLE IF NOT EXISTS route_stations (
route_id INTEGER,
station_id INTEGER,
stop_order INTEGER,
PRIMARY KEY (route_id, stop_order),
FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS trips (
id INTEGER PRIMARY KEY AUTOINCREMENT,
train_id INTEGER,
route_id INTEGER,
departure_time TEXT NOT NULL,
arrival_time TEXT NOT NULL,
delay_minutes INTEGER DEFAULT 0,
FOREIGN KEY (train_id) REFERENCES trains(id),
FOREIGN KEY (route_id) REFERENCES routes(id)
);
CREATE TABLE IF NOT EXISTS bookings (
id INTEGER PRIMARY KEY AUTOINCREMENT,
trip_id INTEGER,
customer_email TEXT NOT NULL,
ticket_count INTEGER NOT NULL,
FOREIGN KEY (trip_id) REFERENCES trips(id)
);
INSERT OR IGNORE INTO stations (id, name, location) VALUES (1, 'Bucuresti Nord', 'Bucuresti'), (2, 'Brasov', 'Brasov'), (3, 'Cluj-Napoca', 'Cluj');
INSERT OR IGNORE INTO trains (id, capacity) VALUES (1, 120), (2, 200);
INSERT OR IGNORE INTO routes (id) VALUES (1);
INSERT OR IGNORE INTO route_stations (route_id, station_id, stop_order) VALUES (1, 1, 0), (1, 2, 1), (1, 3, 2);
INSERT OR IGNORE INTO trips (id, train_id, route_id, departure_time, arrival_time, delay_minutes)
VALUES (1, 1, 1, '06:00', '14:30', 0);
