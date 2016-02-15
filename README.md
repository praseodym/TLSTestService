# TLS protocol state fuzzing

This software can be used to learn state machines of TLS implementations of both clients and servers. It was originally
developed by Joeri de Ruiter for his paper "Protocol state fuzzing of TLS implementations". Original sources were
 downloaded from http://www.cs.bham.ac.uk/~deruitej/download/tlstestservice.zip.

## External Dependencies

- Graphviz (`apt-get install graphviz`)

## Usage

A Gradle build file is included which can be used to build and run the project. To run with Gradle:
`./gradew run -Pconfig=config/openssl_client.properties`.