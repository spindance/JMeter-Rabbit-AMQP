package com.zeroclue.jmeter.protocol.amqp.gui;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.zeroclue.jmeter.protocol.amqp.AMQPSampler;

public abstract class AMQPSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    protected JLabeledTextField exchange = new JLabeledTextField("Exchange");
    private final JCheckBox exchangeDeclare = new JCheckBox("Declare Exchange?", AMQPSampler.DEFAULT_EXCHANGE_DECLARE);
    private final JCheckBox exchangeRedeclare = new JCheckBox("Redeclare?", AMQPSampler.DEFAULT_EXCHANGE_REDECLARE);
    protected JLabeledTextField queue = new JLabeledTextField("Queue");
    protected JLabeledTextField routingKey = new JLabeledTextField("Routing Key");
    protected JLabeledTextField messageTTL = new JLabeledTextField("Message TTL");
    protected JLabeledTextField messageExpires = new JLabeledTextField("Expires");
    protected JLabeledChoice exchangeType = new JLabeledChoice("Exchange Type", new String[]{ "direct", "topic", "headers", "fanout"});
    private final JCheckBox exchangeDurable = new JCheckBox("Durable?", AMQPSampler.DEFAULT_EXCHANGE_DURABLE);
    private final JCheckBox queueDeclare = new JCheckBox("Declare Queue?", AMQPSampler.DEFAULT_QUEUE_DECLARE);
    private final JCheckBox queueDurable = new JCheckBox("Durable?", true);
    private final JCheckBox queueRedeclare = new JCheckBox("Redeclare?", AMQPSampler.DEFAULT_QUEUE_REDECLARE);
    private final JCheckBox queueExclusive = new JCheckBox("Exclusive", true);
    private final JCheckBox queueAutoDelete = new JCheckBox("Auto Delete?", true);
    private final JLabeledTextField iterations = new JLabeledTextField("Number of samples to Aggregate");

    protected abstract void setMainPanel(JPanel panel);

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AMQPSampler)) return;
        AMQPSampler sampler = (AMQPSampler) element;

        exchange.setText(sampler.getExchange());
        exchangeDeclare.setSelected(sampler.getExchangeDeclare());
        exchangeType.setText(sampler.getExchangeType());
        exchangeDurable.setSelected(sampler.getExchangeDurable());
        exchangeRedeclare.setSelected(sampler.getExchangeRedeclare());
        queue.setText(sampler.getQueue());
        routingKey.setText(sampler.getRoutingKey());
        messageTTL.setText(sampler.getMessageTTL());
        messageExpires.setText(sampler.getMessageExpires());

        queueDeclare.setSelected(sampler.getQueueDeclare());
        queueDurable.setSelected(sampler.queueDurable());
        queueExclusive.setSelected(sampler.queueExclusive());
        queueAutoDelete.setSelected(sampler.queueAutoDelete());
        queueRedeclare.setSelected(sampler.getQueueRedeclare());

        iterations.setText(sampler.getIterations());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        exchange.setText("jmeterExchange");
        queue.setText("jmeterQueue");
        exchangeDeclare.setSelected(AMQPSampler.DEFAULT_EXCHANGE_DECLARE);
        exchangeDurable.setSelected(AMQPSampler.DEFAULT_EXCHANGE_DURABLE);
        exchangeRedeclare.setSelected(AMQPSampler.DEFAULT_EXCHANGE_REDECLARE);
        routingKey.setText("jmeterRoutingKey");
        messageTTL.setText("");
        messageExpires.setText("");
        exchangeType.setText("direct");
        queueDeclare.setSelected(AMQPSampler.DEFAULT_QUEUE_DECLARE);
        queueDurable.setSelected(true);
        queueExclusive.setSelected(false);
        queueAutoDelete.setSelected(false);
        queueRedeclare.setSelected(AMQPSampler.DEFAULT_QUEUE_REDECLARE);

        iterations.setText(AMQPSampler.DEFAULT_ITERATIONS_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement element) {
        AMQPSampler sampler = (AMQPSampler) element;
        sampler.clear();
        configureTestElement(sampler);

        sampler.setExchange(exchange.getText());
        sampler.setExchangeDeclare(exchangeDeclare.isSelected());
        sampler.setExchangeDurable(exchangeDurable.isSelected());
        sampler.setExchangeRedeclare(exchangeRedeclare.isSelected());
        sampler.setQueue(queue.getText());
        sampler.setRoutingKey(routingKey.getText());
        sampler.setMessageTTL(messageTTL.getText());
        sampler.setMessageExpires(messageExpires.getText());
        sampler.setExchangeType(exchangeType.getText());
        sampler.setQueueDeclare(queueDeclare.isSelected());
        sampler.setQueueDurable(queueDurable.isSelected());
        sampler.setQueueExclusive(queueExclusive.isSelected());
        sampler.setQueueAutoDelete(queueAutoDelete.isSelected());
        sampler.setQueueRedeclare(queueRedeclare.isSelected());

        sampler.setIterations(iterations.getText());
    }

    protected void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title

        JPanel mainPanel = new VerticalPanel();

        mainPanel.add(makeCommonPanel());

        iterations.setPreferredSize(new Dimension(50,25));
        mainPanel.add(iterations);

        add(mainPanel);

        setMainPanel(mainPanel);
    }

    private Component makeCommonPanel() {
        GridBagConstraints gridBagConstraints, gridBagConstraintsCommon;

        gridBagConstraintsCommon = new GridBagConstraints();
        gridBagConstraintsCommon.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsCommon.anchor = GridBagConstraints.WEST;
        gridBagConstraintsCommon.weightx = 0.5;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        JPanel commonPanel = new JPanel(new GridBagLayout());

        JPanel exchangeSettings = new JPanel(new GridBagLayout());
        exchangeSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Exchange"));
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        exchangeSettings.add(exchange, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        exchangeSettings.add(exchangeType, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        exchangeSettings.add(exchangeDurable, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        exchangeSettings.add(exchangeDeclare, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        exchangeSettings.add(exchangeRedeclare, gridBagConstraints);

        JPanel queueSettings = new JPanel(new GridBagLayout());
        queueSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Queue"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        queueSettings.add(queue, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(routingKey, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        queueSettings.add(messageTTL, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        queueSettings.add(messageExpires, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        queueSettings.add(queueDurable, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        queueSettings.add(queueExclusive, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        queueSettings.add(queueAutoDelete, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        queueSettings.add(queueDeclare, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        queueSettings.add(queueRedeclare, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 0;

        JPanel exchangeQueueSettings = new VerticalPanel();
        exchangeQueueSettings.add(exchangeSettings);
        exchangeQueueSettings.add(queueSettings);

        commonPanel.add(exchangeQueueSettings, gridBagConstraintsCommon);
        return commonPanel;
    }

}
