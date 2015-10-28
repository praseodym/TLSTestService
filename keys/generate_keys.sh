#!/bin/bash
set -ex

# Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)

# Generate DH parameters
openssl dhparam -outform PEM -out dhparams.pem -5 2048

# Generate CA key and certificate
/usr/lib/ssl/misc/CA.sh -newca

# Generate server key and certificate
openssl req -newkey rsa:1024 -nodes -keyout server.key -out server.req
openssl ca -out server.crt -infiles server.req

# Generate client key and certificate
openssl req -newkey rsa:1024 -nodes -keyout client.key -out client.req
openssl ca -out client.crt -infiles client.req

# Generate client DH key and certificate
openssl genpkey -paramfile dhparams.pem -out client_dh.key
openssl pkey -in client_dh.key -pubout -out client_dh.pub
#~/SSL/openssl/openssl-1.0.2/apps/openssl x509 -req -in client.req -CAkey demoCA/private/cakey.pem -CA demoCA/cacert.pem -force_pubkey client_dh.pub -out client_dh.crt -CAcreateserial -extensions v3_req -extfile ./openssl.cnf


# Get keys in Java keystore
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name server -CAfile ca.crt -caname root
keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore keystore -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias server

openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name client -CAfile ca.crt -caname root
keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore keystore -srckeystore client.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias client

#openssl pkcs12 -export -in client_dh.crt -inkey client_dh.key -out client_dh.p12 -name clientdh -CAfile ca.crt -caname root
#keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore keystore -srckeystore client_dh.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias clientdh

# Get keys in Netscape keystore
certutil -N -d .
certutil -A -n ca -i demoCA/cacert.pem -d . -t TC
certutil -A -n server -i server.crt -d . -t P
pk12util -d . -i server.p12
certutil -A -n client1 -i client.crt -d . -t P
pk12util -d . -i client.p12
