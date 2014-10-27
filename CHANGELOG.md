### Changelog ###
- 3.master
    - Add user-id message property field
    - Set response data on consumer to delivery body byte array. Use `getResponseData()` to access
    - Add `setMessageBytes` to publisher to allow publishing of binary data
    - Make TLS truststore a JKS file instead of PKCS#12
    - Add AMQP Connection Manager so AMQP samplers can share a single channel and connection

- 2.master
    - Add checkbox for whether to declare configured queues and exchanges
    - Allow key and trust store fields to take relative paths

- 1.master
    - Add client SSL certificate support
