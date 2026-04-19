#!/bin/bash
#
#  60 - Run web app.sh
#
#  Online Boutique on yugabyteDB
#
#  This script:
#    1. Reads properties.ini for database connection details
#    2. Drops and recreates the database
#    3. Creates all tables and seeds product data
#    4. Builds the Java application (if needed)
#    5. Launches the web application on the port from properties.ini
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ---------------------------------------------------------------
#  Parse properties.ini
# ---------------------------------------------------------------
DB_HOST=""
DB_PORT=""
DB_NAME=""
DB_USER=""
DB_PASSWORD=""

while IFS='=' read -r key value; do
    key=$(echo "$key" | xargs)
    value=$(echo "$value" | xargs)
    [[ -z "$key" || "$key" == \#* || "$key" == \[* ]] && continue
    case "$key" in
        DATABASE_HOST)     DB_HOST="$value" ;;
        DATABASE_PORT)     DB_PORT="$value" ;;
        DATABASE_NAME)     DB_NAME="$value" ;;
        DATABASE_USER)     DB_USER="$value" ;;
        DATABASE_PASSWORD) DB_PASSWORD="$value" ;;
        APPLICATION_PORT)  APP_PORT="$value" ;;
    esac
done < "$SCRIPT_DIR/properties.ini"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="${DB_NAME:-my_db41}"
DB_USER="${DB_USER:-yugabyte}"
APP_PORT="${APP_PORT:-8080}"

echo ""
echo "=============================================="
echo "  Online Boutique on yugabyteDB"
echo "=============================================="
echo "  Host:     $DB_HOST"
echo "  Port:     $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User:     $DB_USER"
echo "=============================================="
echo ""

# ---------------------------------------------------------------
#  Find ysqlsh or psql
# ---------------------------------------------------------------
YSQLSH=""
if command -v ysqlsh &>/dev/null; then
    YSQLSH="ysqlsh"
elif [ -x /opt/yugabyte/bin/ysqlsh ]; then
    YSQLSH="/opt/yugabyte/bin/ysqlsh"
elif command -v psql &>/dev/null; then
    YSQLSH="psql"
else
    echo "ERROR: Neither ysqlsh nor psql found. Cannot set up the database."
    exit 1
fi

echo "Using SQL client: $YSQLSH"

# Build the connection args
CONN_ARGS="-h $DB_HOST -p $DB_PORT -U $DB_USER"
if [ -n "$DB_PASSWORD" ]; then
    export PGPASSWORD="$DB_PASSWORD"
fi

# ---------------------------------------------------------------
#  Recreate the database
# ---------------------------------------------------------------
echo ""
echo "--- Dropping database $DB_NAME (if exists) ---"
$YSQLSH $CONN_ARGS -d yugabyte -c "DROP DATABASE IF EXISTS $DB_NAME;" 2>&1 || true

echo "--- Creating database $DB_NAME ---"
$YSQLSH $CONN_ARGS -d yugabyte -c "CREATE DATABASE $DB_NAME;"

echo "--- Creating tables ---"
$YSQLSH $CONN_ARGS -d "$DB_NAME" -f "$SCRIPT_DIR/online-boutique/src/main/resources/schema.sql"

echo "--- Seeding product data ---"
$YSQLSH $CONN_ARGS -d "$DB_NAME" -f "$SCRIPT_DIR/online-boutique/src/main/resources/data.sql"

echo ""
echo "--- Verifying data ---"
$YSQLSH $CONN_ARGS -d "$DB_NAME" -c "SELECT id, name, price_units || '.' || (price_nanos/10000000) AS price FROM products ORDER BY name;"
echo ""
$YSQLSH $CONN_ARGS -d "$DB_NAME" -c "SELECT email, first_name || ' ' || last_name AS name, city, state FROM users ORDER BY last_name;"

echo ""
echo "Database ready."

# ---------------------------------------------------------------
#  Build the application (if jar is missing or sources changed)
# ---------------------------------------------------------------
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-arm64

JAR="$SCRIPT_DIR/online-boutique/target/online-boutique-1.0.0.jar"

if [ ! -f "$JAR" ]; then
    echo ""
    echo "--- Building the application ---"
    cd "$SCRIPT_DIR/online-boutique"
    ./mvnw package -DskipTests -q
    cd "$SCRIPT_DIR"
    echo "Build complete."
else
    echo ""
    echo "Jar already exists: $JAR"
    echo "(Delete it and re-run to force a rebuild)"
fi

# ---------------------------------------------------------------
#  Launch the application
# ---------------------------------------------------------------
echo ""
echo "=============================================="
echo "  Starting Online Boutique on port $APP_PORT ..."
echo "  Open http://localhost:$APP_PORT in your browser"
echo "  Sign in at http://localhost:$APP_PORT/login"
echo "  Press Ctrl+C to stop"
echo "=============================================="
echo ""

cd "$SCRIPT_DIR/online-boutique"
java -jar target/online-boutique-1.0.0.jar
