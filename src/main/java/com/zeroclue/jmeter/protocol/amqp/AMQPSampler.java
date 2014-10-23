package com.zeroclue.jmeter.protocol.amqp;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.*;
import java.security.*;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;

import com.rabbitmq.client.*;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import org.apache.commons.lang3.StringUtils;

public abstract class AMQPSampler extends AbstractSampler
    implements ThreadListener, ConfigMergabilityIndicator {

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<String>(
            Arrays.asList(new String[]{
                    "com.zeroclue.jmeter.protocol.amqp.gui.AMQPConnectionManagerGui"}));

    public static final boolean DEFAULT_EXCHANGE_DECLARE = false;
    public static final boolean DEFAULT_EXCHANGE_DURABLE = true;
    public static final boolean DEFAULT_EXCHANGE_REDECLARE = false;
    public static final boolean DEFAULT_QUEUE_DECLARE = false;
    public static final boolean DEFAULT_QUEUE_REDECLARE = false;


    public static final int DEFAULT_ITERATIONS = 1;
    public static final String DEFAULT_ITERATIONS_STRING = Integer.toString(DEFAULT_ITERATIONS);

    private static final Logger log = LoggingManager.getLoggerForClass();

    //++ These are JMX names, and must not be changed
    protected static final String EXCHANGE = "AMQPSampler.Exchange";
    protected static final String EXCHANGE_DECLARE = "AMQPSampler.ExchangeDeclare";
    protected static final String EXCHANGE_TYPE = "AMQPSampler.ExchangeType";
    protected static final String EXCHANGE_DURABLE = "AMQPSampler.ExchangeDurable";
    protected static final String EXCHANGE_REDECLARE = "AMQPSampler.ExchangeRedeclare";
    protected static final String QUEUE = "AMQPSampler.Queue";
    protected static final String QUEUE_DECLARE = "AMQPSampler.QueueDeclare";
    protected static final String ROUTING_KEY = "AMQPSampler.RoutingKey";
    private static final String ITERATIONS = "AMQPSampler.Iterations";
    private static final String MESSAGE_TTL = "AMQPSampler.MessageTTL";
    private static final String MESSAGE_EXPIRES = "AMQPSampler.MessageExpires";
    private static final String QUEUE_DURABLE = "AMQPSampler.QueueDurable";
    private static final String QUEUE_REDECLARE = "AMQPSampler.Redeclare";
    private static final String QUEUE_EXCLUSIVE = "AMQPSampler.QueueExclusive";
    private static final String QUEUE_AUTO_DELETE = "AMQPSampler.QueueAutoDelete";

    private transient ConnectionFactory factory;
    private transient Connection connection;

    private transient AMQPConnectionManager connectionManager;

    protected AMQPSampler(){

    }

    @Override
    public void addTestElement(TestElement el) {
        if (el instanceof AMQPConnectionManager) {
            if (getConnectionManager() == null) {
                // We only want the lowest level Connection Manager, ignore the rest.
                setConnectionManager((AMQPConnectionManager) el);
            }
        } else {
            super.addTestElement(el);
        }
    }

    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    protected void setConnectionManager(AMQPConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    protected AMQPConnectionManager getConnectionManager() {
        return connectionManager;
    }

    protected boolean initChannel() throws Exception {
        Channel channel = getChannel();

        if(channel != null && !channel.isOpen()){
            log.warn("channel " + channel.getChannelNumber()
                    + " closed unexpectedly: ", channel.getCloseReason());
            channel = null; // so we re-open it below
        }

        if(channel == null) {
            channel = getConnectionManager().createChannel();
            setChannel(channel);

            //TODO: Break out queue binding
            boolean queueConfigured = (getQueue() != null && !getQueue().isEmpty());

            if(queueConfigured && getQueueDeclare()) {
                if (getQueueRedeclare()) {
                    deleteQueue();
                }

                AMQP.Queue.DeclareOk declareQueueResp = channel.queueDeclare(getQueue(), queueDurable(), queueExclusive(), queueAutoDelete(), getQueueArguments());
            }

            if(!StringUtils.isBlank(getExchange()) && getExchangeDeclare()) { //Use a named exchange
                if (getExchangeRedeclare()) {
                    deleteExchange();
                }

                AMQP.Exchange.DeclareOk declareExchangeResp = channel.exchangeDeclare(getExchange(), getExchangeType(), getExchangeDurable());
            }

            if (queueConfigured) {
              channel.queueBind(getQueue(), getExchange(), getRoutingKey());
            }

            log.info("bound to:"
                +"\n\t queue: " + getQueue()
                +"\n\t exchange: " + getExchange()
                +"\n\t exchange(D)? " + getExchangeDurable()
                +"\n\t routing key: " + getRoutingKey()
                +"\n\t arguments: " + getQueueArguments()
                );

        }
        return true;
    }

    private Map<String, Object> getQueueArguments() {
        Map<String, Object> arguments = new HashMap<String, Object>();

        if(getMessageTTL() != null && !getMessageTTL().isEmpty())
            arguments.put("x-message-ttl", getMessageTTLAsInt());

        if(getMessageExpires() != null && !getMessageExpires().isEmpty())
            arguments.put("x-expires", getMessageExpiresAsInt());

        return arguments;
    }

    protected abstract Channel getChannel();
    protected abstract void setChannel(Channel channel);

    // TODO: make this configurable
    protected BasicProperties getProperties() {
        AMQP.BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN;
        return properties;
    }

    /**
     * @return a string for the sampleResult Title
     */
    protected String getTitle() {
        return this.getName();
    }

    public String getIterations() {
        return getPropertyAsString(ITERATIONS, DEFAULT_ITERATIONS_STRING);
    }

    public void setIterations(String s) {
        setProperty(ITERATIONS, s);
    }

    public int getIterationsAsInt() {
        return getPropertyAsInt(ITERATIONS);
    }

    public String getExchange() {
        return getPropertyAsString(EXCHANGE);
    }

    public void setExchange(String name) {
        setProperty(EXCHANGE, name);
    }

    public boolean getExchangeDeclare() {
        return getPropertyAsBoolean(EXCHANGE_DECLARE);
    }

    public void setExchangeDeclare(boolean exchangeDeclare) {
        setProperty(EXCHANGE_DECLARE, exchangeDeclare);
    }

    public boolean getExchangeDurable() {
        return getPropertyAsBoolean(EXCHANGE_DURABLE);
    }

    public void setExchangeDurable(boolean durable) {
        setProperty(EXCHANGE_DURABLE, durable);
    }

    public String getExchangeType() {
        return getPropertyAsString(EXCHANGE_TYPE);
    }

    public void setExchangeType(String name) {
        setProperty(EXCHANGE_TYPE, name);
    }

    public Boolean getExchangeRedeclare() {
        return getPropertyAsBoolean(EXCHANGE_REDECLARE);
    }

    public void setExchangeRedeclare(Boolean content) {
        setProperty(EXCHANGE_REDECLARE, content);
    }

    public String getQueue() {
        return getPropertyAsString(QUEUE);
    }

    public void setQueue(String name) {
        setProperty(QUEUE, name);
    }

    public boolean getQueueDeclare() {
        return getPropertyAsBoolean(QUEUE_DECLARE);
    }

    public void setQueueDeclare(boolean queueDeclare) {
        setProperty(QUEUE_DECLARE, queueDeclare);
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String name) {
        setProperty(ROUTING_KEY, name);
    }

    public String getMessageTTL() {
        return getPropertyAsString(MESSAGE_TTL);
    }

    public void setMessageTTL(String name) {
        setProperty(MESSAGE_TTL, name);
    }

    protected Integer getMessageTTLAsInt() {
        if (getPropertyAsInt(MESSAGE_TTL) < 1) {
            return null;
        }
        return getPropertyAsInt(MESSAGE_TTL);
    }

    public String getMessageExpires() {
        return getPropertyAsString(MESSAGE_EXPIRES);
    }

    public void setMessageExpires(String name) {
        setProperty(MESSAGE_EXPIRES, name);
    }

    protected Integer getMessageExpiresAsInt() {
        if (getPropertyAsInt(MESSAGE_EXPIRES) < 1) {
            return null;
        }
        return getPropertyAsInt(MESSAGE_EXPIRES);
    }

    /**
     * @return the whether or not the queue is durable
     */
    public String getQueueDurable() {
        return getPropertyAsString(QUEUE_DURABLE);
    }

    public void setQueueDurable(String content) {
        setProperty(QUEUE_DURABLE, content);
    }

    public void setQueueDurable(Boolean value) {
        setProperty(QUEUE_DURABLE, value.toString());
    }

    public boolean queueDurable(){
        return getPropertyAsBoolean(QUEUE_DURABLE);
    }

    /**
     * @return the whether or not the queue is exclusive
     */
    public String getQueueExclusive() {
        return getPropertyAsString(QUEUE_EXCLUSIVE);
    }

    public void setQueueExclusive(String content) {
        setProperty(QUEUE_EXCLUSIVE, content);
    }

    public void setQueueExclusive(Boolean value) {
        setProperty(QUEUE_EXCLUSIVE, value.toString());
    }

    public boolean queueExclusive(){
        return getPropertyAsBoolean(QUEUE_EXCLUSIVE);
    }

    /**
     * @return the whether or not the queue should auto delete
     */
    public String getQueueAutoDelete() {
        return getPropertyAsString(QUEUE_AUTO_DELETE);
    }

    public void setQueueAutoDelete(String content) {
        setProperty(QUEUE_AUTO_DELETE, content);
    }

    public void setQueueAutoDelete(Boolean value) {
        setProperty(QUEUE_AUTO_DELETE, value.toString());
    }

    public boolean queueAutoDelete(){
        return getPropertyAsBoolean(QUEUE_AUTO_DELETE);
    }

    public Boolean getQueueRedeclare() {
        return getPropertyAsBoolean(QUEUE_REDECLARE);
    }

    public void setQueueRedeclare(Boolean content) {
       setProperty(QUEUE_REDECLARE, content);
    }

    protected void cleanup() {
        // try {
        //     //getChannel().close();   // closing the connection will close the channel if it's still open
        //     if(connection != null && connection.isOpen())
        //         connection.close();
        // } catch (IOException e) {
        //     log.error("Failed to close connection", e);
        // }
    }

    @Override
    public void threadFinished() {
        log.info("AMQPSampler.threadFinished called");
        cleanup();
    }

    @Override
    public void threadStarted() {

    }

    protected void deleteQueue() throws Exception {
        // use a different channel since channel closes on exception.
        Channel channel = getConnectionManager().createChannel();
        try {
            log.info("Deleting queue " + getQueue());
            channel.queueDelete(getQueue());
        }
        catch(Exception ex) {
            log.debug(ex.toString(), ex);
            // ignore it.
        }
        finally {
            if (channel.isOpen())  {
                channel.close();
            }
        }
    }

    protected void deleteExchange() throws Exception {
        // use a different channel since channel closes on exception.
        Channel channel = getConnectionManager().createChannel();
        try {
            log.info("Deleting exchange " + getExchange());
            channel.exchangeDelete(getExchange());
        }
        catch(Exception ex) {
            log.debug(ex.toString(), ex);
            // ignore it.
        }
        finally {
            if (channel.isOpen())  {
                channel.close();
            }
        }
    }
}
