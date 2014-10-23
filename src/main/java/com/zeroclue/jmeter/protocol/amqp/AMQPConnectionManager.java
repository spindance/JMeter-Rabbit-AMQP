package com.zeroclue.jmeter.protocol.amqp;

import java.io.IOException;
import java.security.*;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.util.JOrphanUtils;

//
import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.config.ConfigElement;

import com.rabbitmq.client.*;

/**
 * This class is used to manage an AMQP connect. This allows multiple AMQP samples to use the
 * same connection.
 */
public class AMQPConnectionManager extends ConfigTestElement
    implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();

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
        // mergeIn((TestElement) config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean expectsModification() {
        // We don't want to clone the AMQPConnectionManager for each sampler
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
}
