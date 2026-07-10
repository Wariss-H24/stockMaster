#!/bin/sh
# Convertit l'URL Render postgres:// → jdbc:postgresql:// si nécessaire

if [ -n "$DATABASE_URL" ]; then
  # Render donne : postgres://user:pass@host:5432/dbname
  # Spring attend : jdbc:postgresql://host:5432/dbname?user=user&password=pass
  # On convertit simplement le préfixe
  export SPRING_DATASOURCE_URL=$(echo "$DATABASE_URL" | sed 's|postgres://\([^:]*\):\([^@]*\)@\(.*\)|jdbc:postgresql://\3?user=\1\&password=\2|')
  echo "✓ DATABASE_URL convertie en JDBC"
fi

exec java \
  -Xmx400m \
  -Xms200m \
  -XX:+UseContainerSupport \
  -Dspring.profiles.active=prod \
  -jar app.jar
