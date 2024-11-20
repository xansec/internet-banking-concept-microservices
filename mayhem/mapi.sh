#!/bin/bash

set -e
set -x

nohup rust-openid-token-plugin \
--api-under-test-header-name "Authorization" \
--api-under-test-header-prefix "Bearer " \
--oauth-token-url "http://172.25.0.11:8080/realms/javatodev-internet-banking/protocol/openid-connect/token" \
--data-urlencode "grant_type=password" \
--data-urlencode "scope=email" \
--data-urlencode "client_id=javatodev-internet-banking-api-client" \
--data-urlencode "client_secret=nVKeKJcoPbixsy2mxh9Yiy9azzGlA1V0" \
--data-urlencode "username=ib_admin@javatodev.com" \
--data-urlencode "password=5V7huE3G86uB" > nohup.out &

PLUGIN_PID=$!
echo $PLUGIN_PID

mapi run internet-banking-concept-microservices/api 1m ./postman_collection/JAVA_TO_DEV_MICROSERVICES.postman_collection_updated.json --postman-environment-id ./postman_collection/BANKING_CORE_MICROSERVICES_PROJECT.postman_environment_updated.json --url http://172.25.0.6:8082 --header 'Content-Type: application/json' --rewrite-plugin http://localhost:50051 --interactive

kill -9 $PLUGIN_PID || true
