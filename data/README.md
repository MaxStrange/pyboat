# Diplomacy Dataset

This file describes the Diplomacy dataset as found in the diplomacy.sql.gz file.

The dataset consists of 21,197 different games collected from playdiplomacy.com. Each
game is a classic ruleset, classic board that starts with seven active (and distinct)
players. In almost every game of diplomacy ever played, people often stop playing after
a certain point, and these games are no different - but they all at least *start*
with seven players.

**NOTE**
Game 115915 should be deleted from the database via:

```sql
DELETE FROM players WHERE game_id=115915;
DELETE FROM units WHERE game_id=115915;
DELETE FROM orders WHERE game_id=115915;
DELETE FROM turns WHERE game_id=115915;
DELETE FROM games WHERE id=115915;
```
This game was discovered to be anomolous after the database was distributed.

## Tables

There are five tables in total: games, orders, players, turns, units. Below is an
in-depth description of each one.

### games

| Field       | Type             | Null | Key | Default | Extra |
|-------------|------------------|------|-----|---------|-------|
| id          | int(10) unsigned | NO   | PRI | NULL    |       |
| num_turns   | int(10) unsigned | NO   |     | NULL    |       |
| num_players | int(10) unsigned | NO   |     | NULL    |       |

The 'games' table contains three columns: id, num_turns, num_players. The 'id' field
is a unique unsigned int that is used as the primary key for all the games.

There are 21,197 distinct games.

The 'num_players' field is how many players were still actively inputting units at the end of the game.

### orders

| Field       | Type             | Null | Key | Default | Extra |
|-------------|------------------|------|-----|---------|-------|
| game_id     | int(10) unsigned | NO   | MUL | NULL    |       |
| unit_id     | int(10) unsigned | NO   | MUL | NULL    |       |
| unit_order  | varchar(10)      | NO   |     | NULL    |       |
| location    | varchar(100)     | NO   |     | NULL    |       |
| target      | varchar(100)     | YES  |     | NULL    |       |
| target_dest | varchar(100)     | YES  |     | NULL    |       |
| success     | tinyint(1)       | NO   |     | NULL    |       |
| reason      | varchar(255)     | YES  |     | NULL    |       |
| turn_num    | int(10) unsigned | NO   |     | NULL    |       |

The 'orders' table contains the following columns:
* game_id: The 'id' from the 'games' table that identifies which game this order is from
* unit_id: A unique unsigned int for a unit PER GAME (that is, only unique within a single game)
* unit_order: The actual order
* location: The location of the unit at the start of the turn (their origin)
* target: If the order takes a unit as a target, the location where that unit was to be found
* target_dest: If the order is a SUPPORT order, the target_dest is either a) target, in which case it is a SUPPORT HOLD (e.g. Fleet North Sea supports Army Belgium to hold) or b) a location name different from target, in which case
* is a SUPPORT to MOVE (e.g. F Black Sea supports A Bulgaria move to Rumania)
* success: 1 = TRUE, 0 = FALSE. If FALSE, there should be a reason.
* reason: NULL unless success = 0. If Non-NULL, contains a reason for the failure of the order, such as destroyed by A Rumania - Bulgaria' or 'Attack broken by F English Channel - Belgium' or 'Bounced'
* turn_num: Which turn this order was written for: starts at 1, which indicates that the order was written to be carried out in Spring 1901 (the first order resolution phase of the game)

A little more explanation for some of these:

#### unit_order

Possible values for 'unit_order' are:

* MOVE ('location' is the location of the unit, 'target' is the location to move to, 'target_dest' is NULL)
* HOLD ('location' is the location of the unit, 'target' and 'target_dest' are NULL)
* CONVOY ('target' and 'target_dest' are filled in with the unit location and the end goal location, respectively)
* SUPPORT ('target' and 'target_dest' are filled in with the unit to be supported and either their target location (if they are MOVE'ing) or their own location (if they are not MOVE'ing))
* BUILD ('location' is an empty string and 'target' is a string of the form 'fleet|army location' - e.g. 'fleet Berlin')
* RETREAT ('location' and 'target' are filled in with the location of the unit and the target location)
* DESTROY ('location' is filled in with the location of the unit to be destroyed and 'target' is an empty string)

#### location

Possible values for 'location', 'target', and 'target_dest' are:

| location                     |
|------------------------------|
| Edinburgh                    |
| Liverpool                    |
| London                       |
| Marseilles                   |
| Paris                        |
| Brest                        |
| Venice                       |
| Rome                         |
| Naples                       |
| Munich                       |
| Berlin                       |
| Kiel                         |
| Vienna                       |
| Trieste                      |
| Budapest                     |
| Constantinople               |
| Ankara                       |
| Smyrna                       |
| Moscow                       |
| St. Petersburg (South Coast) |
| Warsaw                       |
| Sevastopol                   |
| Norwegian Sea                |
| Yorkshire                    |
| North Sea                    |
| Spain                        |
| Picardy                      |
| Mid-Atlantic Ocean           |
| Tyrrhenian Sea               |
| Silesia                      |
| Holland                      |
| Albania                      |
| Serbia                       |
| Bulgaria                     |
| St. Petersburg               |
| Gulf of Bothnia              |
| Ukraine                      |
|                              |
| Norway                       |
| Belgium                      |
| Portugal                     |
| Piedmont                     |
| Tunis                        |
| Galicia                      |
| Greece                       |
| Black Sea                    |
| Rumania                      |
| Sweden                       |
| Finland                      |
| Helgoland Bight              |
| Burgundy                     |
| English Channel              |
| Tyrolia                      |
| Ionian Sea                   |
| Denmark                      |
| Adriatic Sea                 |
| Wales                        |
| Irish Sea                    |
| Armenia                      |
| St. Petersburg (North Coast) |
| North Atlantic Ocean         |
| Ruhr                         |
| Apulia                       |
| Aegean Sea                   |
| Skagerrack                   |
| Gascony                      |
| Prussia                      |
| Barents Sea                  |
| Bohemia                      |
| Western Mediterranean        |
| Tuscany                      |
| Baltic Sea                   |
| Gulf of Lyons                |
| Eastern Mediterranean        |
| Spain (North Coast)          |
| Livonia                      |
| Syria                        |
| Clyde                        |
| North Africa                 |
| Spain (South Coast)          |
| Bulgaria (South Coast)       |
| Bulgaria (East Coast)        |

Note that location can only be an empty string when the unit_order is BUILD.

### players

| Field              | Type             | Null | Key | Default | Extra |
|--------------------|------------------|------|-----|---------|-------|
| game_id            | int(10) unsigned | NO   | MUL | NULL    |       |
| country            | char(1)          | NO   |     | NULL    |       |
| won                | tinyint(1)       | NO   |     | NULL    |       |
| num_supply_centers | int(10) unsigned | NO   |     | NULL    |       |
| eliminated         | tinyint(1)       | NO   |     | NULL    |       |
| start_turn         | int(10) unsigned | NO   |     | NULL    |       |
| end_turn           | int(10) unsigned | NO   |     | NULL    |       |

* 'game_id' is the 'id' from the 'games' table that this player is from
* 'country' is one of: {E, F, I, G, A, T, R} representing {England, France, Italy, Germany, Austria, Turkey, Russia}
* 'won' is a 1 if the player won the game with the given game_id, 0 otherwise; it is possible for more than one person to win; winning is defined as having the most supply centers at the end of the game
* 'num_supply_centers' is an unsigned int from 0 to 34 and is the number of supply centers the player ended the game with
* eliminated: 1 if true, 0 if false
* start_turn: The turn the player actually started to play
* end_turn: The last turn the player actually input orders

### turns

| Field       | Type             | Null | Key | Default | Extra |
|-------------|------------------|------|-----|---------|-------|
| game_id     | int(10) unsigned | NO   | MUL | NULL    |       |
| turn_num    | int(10) unsigned | NO   |     | NULL    |       |
| phase       | varchar(10)      | NO   |     | NULL    |       |
| year        | int(10) unsigned | NO   |     | NULL    |       |
| season      | varchar(10)      | NO   |     | NULL    |       |
| scs_england | int(10) unsigned | NO   |     | NULL    |       |
| scs_france  | int(10) unsigned | NO   |     | NULL    |       |
| scs_italy   | int(10) unsigned | NO   |     | NULL    |       |
| scs_russia  | int(10) unsigned | NO   |     | NULL    |       |
| scs_turkey  | int(10) unsigned | NO   |     | NULL    |       |
| scs_germany | int(10) unsigned | NO   |     | NULL    |       |
| scs_austria | int(10) unsigned | NO   |     | NULL    |       |

* game_id: The 'id' from the 'games' table
* turn_num: An unsigned int unique for a given game_id, starting at 0 for each game; 0 (is Winter 1900 - no orders are executed on it)
* phase: {'Winter', 'Orders', 'Build', 'Retreat'}
* year: Starts from 1900 and increases by one in every Spring
* season: {'Winter', 'Spring', 'Fall'}
* scs_x: The number (from 0 to 34) of supply centers x has at the start of this turn.

### units

| Field      | Type             | Null | Key | Default | Extra |
|------------|------------------|------|-----|---------|-------|
| game_id    | int(10) unsigned | NO   | MUL | NULL    |       |
| country    | char(1)          | NO   |     | NULL    |       |
| type       | char(1)          | NO   |     | NULL    |       |
| start_turn | int(10) unsigned | NO   |     | NULL    |       |
| end_turn   | int(10) unsigned | NO   |     | NULL    |       |
| unit_id    | int(10) unsigned | NO   |     | NULL    |       |

* game_id: The 'id' from the 'games' table
* country: One of: {E, F, I, G, A, T, R} representing {England, France, Italy, Germany, Austria, Turkey, Russia}; this is the owner of the unit
* type: One of {'F' or 'A'} for {Fleet, Army}
* start_turn: The first turn the unit was present (0 for Winter 1900 - i.e. starts on the board)
* end_turn: The last turn the unit was present on the board
* unit_id: A unique ID within a game

