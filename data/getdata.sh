# This script gets the most up-to-date data from the remote SQL server and saves it on this computer.

# First you should run:
# mysqldump -u root -p diplomacy > diplomacy.sql
# on the remote server

scp -P 2000 ubuntu@73.221.142.183:diplomacy.sql ./
mysql --user=root --database=diplomacy < diplomacy.sql

