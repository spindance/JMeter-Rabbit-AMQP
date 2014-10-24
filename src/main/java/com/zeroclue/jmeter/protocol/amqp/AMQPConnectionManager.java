package com.zeroclue.jmeter.protocol.amqp;

import java.security.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.ConfigElement;

import com.rabbitmq.client.*;

/**
 * This class is used to manage an AMQP connect. This allows multiple AMQP samples to use the
 * same connection.
 */
public class AMQPConnectionManager extends ConfigTestElement
    implements ThreadListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    // these are hard-coded for now, should eventually be configurable
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String TRUSTSTORE_TYPE = "JKS";
    private static final String CERT_TYPE = "SunX509";
    private static final String SSL_VERSION = "TLSv1.2";

    public static final int DEFAULT_PORT = 5672;
    public static final String DEFAULT_PORT_STRING = Integer.toString(DEFAULT_PORT);

    public static final int DEFAULT_TIMEOUT = 1000;
    public static final String DEFAULT_TIMEOUT_STRING = Integer.toString(DEFAULT_TIMEOUT);

    public static final int DEFAULT_HEARTBEAT = 1;
    public static final String DEFAULT_HEARTBEAT_STRING = Integer.toString(DEFAULT_HEARTBEAT);

    private static final String VIRUTAL_HOST = "AMQPConnectionManager.VirtualHost";
    private static final String HOST = "AMQPConnectionManager.Host";
    private static final String PORT = "AMQPConnectionManager.Port";
    private static final String SSL = "AMQPConnectionManager.SSL";
    private static final String SSL_CLIENT_CERT = "AMQPConnectionManager.SSLClientCert";
    private static final String USERNAME = "AMQPConnectionManager.Username";
    private static final String PASSWORD = "AMQPConnectionManager.Password";
    private static final String PATH_TO_KEY_STORE = "AMQPConnectionManager.PathToKeyStore";
    private static final String KEY_STORE_PASSWORD = "AMQPConnectionManager.KeyStorePassword";
    private static final String PATH_TO_TRUST_STORE = "AMQPConnectionManager.PathToTrustStore";
    private static final String TRUST_STORE_PASSWORD = "AMQPConnectionManager.TrustStorePassword";
    private static final String TIMEOUT = "AMQPConnectionManager.Timeout";
    private static final String HEARTBEAT = "AMQPConnectionManager.Heartbeat";

    private transient ConnectionFactory factory;
    private transient Connection connection;

    public AMQPConnectionManager() {
        factory = new ConnectionFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConfigElement(ConfigElement config) {
        // Can't figure out when/how this would be called, but it doesn't seem like we want it to
        // do anything anyway.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean expectsModification() {
        // Don't clone the AMQPConnectionManager for each sampler
        return true;
    }

    public String getVirtualHost() {
        return getPropertyAsString(VIRUTAL_HOST);
    }

    public void setVirtualHost(String name) {
        setProperty(VIRUTAL_HOST, name);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setHost(String name) {
        setProperty(HOST, name);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    public void setPort(String name) {
        setProperty(PORT, name);
    }

    protected int getPortAsInt() {
        if (getPropertyAsInt(PORT) < 1) {
            return DEFAULT_PORT;
        }
        return getPropertyAsInt(PORT);
    }

    public void setConnectionSSL(String content) {
        setProperty(SSL, content);
    }

    public void setConnectionSSL(Boolean value) {
        setProperty(SSL, value.toString());
    }

    public boolean connectionSSL() {
        return getPropertyAsBoolean(SSL);
    }

    public void setSSLClientCert(String content) {
        setProperty(SSL_CLIENT_CERT, content);
    }

    public void setSSLClientCert(Boolean value) {
        setProperty(SSL_CLIENT_CERT, value.toString());
    }

    public boolean sslClientCert() {
        return getPropertyAsBoolean(SSL_CLIENT_CERT);
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    public void setUsername(String name) {
        setProperty(USERNAME, name);
    }


    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    public void setPassword(String name) {
        setProperty(PASSWORD, name);
    }

    public String getPathToKeyStore() {
        return getPropertyAsString(PATH_TO_KEY_STORE);
    }

    public void setPathToKeyStore(String pathToKeyStore) {
        setProperty(PATH_TO_KEY_STORE, pathToKeyStore);
    }

    public String getKeyStorePassword() {
        return getPropertyAsString(KEY_STORE_PASSWORD);
    }

    public void setKeyStorePassword(String keyStorePassword) {
        setProperty(KEY_STORE_PASSWORD, keyStorePassword);
    }

    public String getPathToTrustStore() {
        return getPropertyAsString(PATH_TO_TRUST_STORE);
    }

    public void setPathToTrustStore(String pathToTrustStore) {
        setProperty(PATH_TO_TRUST_STORE, pathToTrustStore);
    }

    public String getTrustStorePassword() {
        return getPropertyAsString(TRUST_STORE_PASSWORD);
    }

    public void setTrustStorePassword(String trustStorePassword) {
        setProperty(TRUST_STORE_PASSWORD, trustStorePassword);
    }

    protected int getTimeoutAsInt() {
        if (getPropertyAsInt(TIMEOUT) < 1) {
            return DEFAULT_TIMEOUT;
        }
        return getPropertyAsInt(TIMEOUT);
    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT, DEFAULT_TIMEOUT_STRING);
    }

    public void setTimeout(String s) {
        setProperty(TIMEOUT, s);
    }

    protected int getHeartbeatAsInt() {
        if (getPropertyAsInt(HEARTBEAT) < 1) {
            return DEFAULT_HEARTBEAT;
        }
        return getPropertyAsInt(HEARTBEAT);
    }

    public String getHeartbeat() {
        return getPropertyAsString(HEARTBEAT, DEFAULT_HEARTBEAT_STRING);
    }

    public void setHeartbeat(String s) {
        setProperty(HEARTBEAT, s);
    }

    @Override
    public void threadStarted() {

    }

    @Override
    public void threadFinished() {
        log.info("AMQPConnectionManager.threadFinished called");
        cleanup();
    }

    public Channel createChannel() throws Exception {
        log.info("Creating channel " + getVirtualHost()+":"+getPortAsInt());

         Channel channel = getConnection().createChannel();
         if(!channel.isOpen()){
            log.fatalError("Failed to open channel: " + channel.getCloseReason().getLocalizedMessage());
         }
        return channel;
    }

    public Connection getConnection() throws Exception {
        if (connection == null || !connection.isOpen()) {

            factory.setVirtualHost(getVirtualHost());
            factory.setUsername(getUsername());
            factory.setPassword(getPassword());
            factory.setConnectionTimeout(getTimeoutAsInt());
            factory.setRequestedHeartbeat(getHeartbeatAsInt());
            if (connectionSSL()) {
                if (sslClientCert()) {
                    factory.useSslProtocol(getSslContext());
                } else {
                    factory.useSslProtocol("TLS");
                }
            }

            log.info("RabbitMQ ConnectionFactory using:"
                  +"\n\t virtual host: " + getVirtualHost()
                  +"\n\t host: " + getHost()
                  +"\n\t port: " + getPort()
                  +"\n\t username: " + getUsername()
                  +"\n\t password: " + getPassword()
                  +"\n\t timeout: " + getTimeout()
                  +"\n\t heartbeat: " + getHeartbeat()
                  +"\nin " + this
                  );

            String[] hosts = getHost().split(",");
            Address[] addresses = new Address[hosts.length];
            for (int i = 0; i < hosts.length; i++) {
                addresses[i] = new Address(hosts[i], getPortAsInt());
            }
            log.info("Using hosts: " + Arrays.toString(hosts) + " addresses: " + Arrays.toString(addresses));
            connection = factory.newConnection(addresses);
        }
        return connection;
    }

    protected void cleanup() {
        try {
            if(connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException e) {
            log.error("Failed to close connection", e);
        }
    }

    private SSLContext getSslContext() throws Exception {
        SSLContext c = SSLContext.getInstance(SSL_VERSION);

        // use the default SecureRandom implementation by setting the third param to null
        c.init(getKeyManagers(), getTrustManagers(), null);

        return c;
    }

    private KeyManager[] getKeyManagers() throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(CERT_TYPE);
        kmf.init(getKeyStore(getPathToKeyStore(), getKeyStorePassword(), KEYSTORE_TYPE), getKeyStorePassword().toCharArray());

        return kmf.getKeyManagers();
    }

    private TrustManager[] getTrustManagers() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(CERT_TYPE);
        tmf.init(getKeyStore(getPathToTrustStore(), getTrustStorePassword(), TRUSTSTORE_TYPE));

        return tmf.getTrustManagers();
    }

    private KeyStore getKeyStore(String path, String pass, String keyStoreType) throws Exception {
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(new FileInputStream(Paths.get(path).toAbsolutePath().toString()), pass.toCharArray());
        return ks;
    }
}
