#!/bin/sh
set -eu

if [ -z "${JWT_SECRET:-}" ]; then
    secret_file=/opt/jboss/wildfly/standalone/data/openf-secrets/jwt-secret
    if [ ! -s "$secret_file" ]; then
        umask 077
        head -c 48 /dev/urandom | base64 | tr -d '\n' > "$secret_file"
    fi
    JWT_SECRET=$(cat "$secret_file")
    export JWT_SECRET
fi

if [ "${#JWT_SECRET}" -lt 32 ]; then
    echo 'JWT_SECRET debe contener al menos 32 caracteres.' >&2
    exit 1
fi

exec "$JBOSS_HOME/bin/standalone.sh" "$@"
