-- Demo catalogue: Pokémon #1 to #20 with base data captured from PokeAPI.
-- Own fields (local_name, region, tags) are set for a few of them to show editing.

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (1, 'bulbasaur', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png', 'Seed Pokémon', 7, 69, 'Semillita', 'Kanto', 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 1), 0, 'grass', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 1), 1, 'poison', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 1), 0, 'overgrow', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 1), 1, 'chlorophyll', TRUE);
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 1), 0, 'starter');
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 1), 1, 'grass');

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (2, 'ivysaur', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/2.png', 'Seed Pokémon', 10, 130, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 2), 0, 'grass', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 2), 1, 'poison', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 2), 0, 'overgrow', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 2), 1, 'chlorophyll', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (3, 'venusaur', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/3.png', 'Seed Pokémon', 20, 1000, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 3), 0, 'grass', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 3), 1, 'poison', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 3), 0, 'overgrow', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 3), 1, 'chlorophyll', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (4, 'charmander', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png', 'Lizard Pokémon', 6, 85, 'Llamita', 'Kanto', 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 4), 0, 'fire', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 4), 0, 'blaze', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 4), 1, 'solar-power', TRUE);
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 4), 0, 'starter');
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 4), 1, 'fire');

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (5, 'charmeleon', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/5.png', 'Flame Pokémon', 11, 190, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 5), 0, 'fire', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 5), 0, 'blaze', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 5), 1, 'solar-power', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (6, 'charizard', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png', 'Flame Pokémon', 17, 905, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 6), 0, 'fire', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 6), 1, 'flying', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 6), 0, 'blaze', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 6), 1, 'solar-power', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (7, 'squirtle', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png', 'Tiny Turtle Pokémon', 5, 90, 'Colita', 'Kanto', 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 7), 0, 'water', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 7), 0, 'torrent', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 7), 1, 'rain-dish', TRUE);
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 7), 0, 'starter');
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 7), 1, 'water');

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (8, 'wartortle', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/8.png', 'Turtle Pokémon', 10, 225, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 8), 0, 'water', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 8), 0, 'torrent', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 8), 1, 'rain-dish', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (9, 'blastoise', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/9.png', 'Shellfish Pokémon', 16, 855, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 9), 0, 'water', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 9), 0, 'torrent', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 9), 1, 'rain-dish', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (10, 'caterpie', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/10.png', 'Worm Pokémon', 3, 29, 'Oruga', 'Kanto', 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 10), 0, 'bug', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 10), 0, 'shield-dust', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 10), 1, 'run-away', TRUE);
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 10), 0, 'bug');

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (11, 'metapod', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/11.png', 'Cocoon Pokémon', 7, 99, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 11), 0, 'bug', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 11), 0, 'shed-skin', FALSE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (12, 'butterfree', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/12.png', 'Butterfly Pokémon', 11, 320, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 12), 0, 'bug', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 12), 1, 'flying', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 12), 0, 'compound-eyes', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 12), 1, 'tinted-lens', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (13, 'weedle', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/13.png', 'Hairy Bug Pokémon', 3, 32, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 13), 0, 'bug', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 13), 1, 'poison', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 13), 0, 'shield-dust', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 13), 1, 'run-away', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (14, 'kakuna', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/14.png', 'Cocoon Pokémon', 6, 100, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 14), 0, 'bug', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 14), 1, 'poison', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 14), 0, 'shed-skin', FALSE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (15, 'beedrill', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/15.png', 'Poison Bee Pokémon', 10, 295, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 15), 0, 'bug', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 15), 1, 'poison', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 15), 0, 'swarm', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 15), 1, 'sniper', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (16, 'pidgey', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/16.png', 'Tiny Bird Pokémon', 3, 18, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 16), 0, 'normal', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 16), 1, 'flying', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 16), 0, 'keen-eye', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 16), 1, 'tangled-feet', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 16), 2, 'big-pecks', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (17, 'pidgeotto', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/17.png', 'Bird Pokémon', 11, 300, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 17), 0, 'normal', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 17), 1, 'flying', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 17), 0, 'keen-eye', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 17), 1, 'tangled-feet', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 17), 2, 'big-pecks', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (18, 'pidgeot', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/18.png', 'Bird Pokémon', 15, 395, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 18), 0, 'normal', 1);
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 18), 1, 'flying', 2);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 18), 0, 'keen-eye', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 18), 1, 'tangled-feet', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 18), 2, 'big-pecks', TRUE);

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (19, 'rattata', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/19.png', 'Mouse Pokémon', 3, 35, 'Ratita', 'Kanto', 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 19), 0, 'normal', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 19), 0, 'run-away', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 19), 1, 'guts', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 19), 2, 'hustle', TRUE);
INSERT INTO pokemon_tag (pokemon_id, position, tag) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 19), 0, 'normal');

INSERT INTO pokemon (pokeapi_id, name, sprite_url, category, height, weight, local_name, region, version, created_at, updated_at) VALUES (20, 'raticate', 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/20.png', 'Mouse Pokémon', 7, 185, NULL, NULL, 0, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
INSERT INTO pokemon_type (pokemon_id, position, name, slot) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 20), 0, 'normal', 1);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 20), 0, 'run-away', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 20), 1, 'guts', FALSE);
INSERT INTO pokemon_ability (pokemon_id, position, name, hidden) VALUES ((SELECT id FROM pokemon WHERE pokeapi_id = 20), 2, 'hustle', TRUE);
