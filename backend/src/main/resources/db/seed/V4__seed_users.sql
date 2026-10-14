-- Demo accounts. Passwords are documented in README ("Credenciales demo"); only BCrypt hashes are stored.
--   admin / Admin123!  -> ADMIN
--   demo  / Demo1234!  -> USER

INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin', 'admin@pokedex.local', '$2a$10$drW.d8p24HzTofuFzltxNe4pd7JO229x1GdBnpJ/kAQ4z6M.RB6B6', 'ADMIN'),
    ('demo',  'demo@pokedex.local',  '$2a$10$H8G6cbcnzc2VrzBhndq2n.79bfujIwm1K.ywDkRpSHKYYYvyywqYK', 'USER');
