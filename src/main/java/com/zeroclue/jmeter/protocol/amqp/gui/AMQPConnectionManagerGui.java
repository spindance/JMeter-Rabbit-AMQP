package com.zeroclue.jmeter.protocol.amqp.gui;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.zeroclue.jmeter.protocol.amqp.AMQPConnectionManager;

/**
 * A GUI component allowing the user to configur an AMQP connection
 */
public class AMQPConnectionManagerGui extends AbstractConfigGui {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private JLabeledTextField virtualHost = new JLabeledTextField("Virtual Host");
    private JLabeledTextField host = new JLabeledTextField("Host");
    private JLabeledTextField port = new JLabeledTextField("Port");
    private JLabeledTextField username = new JLabeledTextField("Username");
    private JLabeledTextField password = new JLabeledTextField("Password");
    private JLabeledTextField pathToKeyStore = new JLabeledTextField("Path to Key Store");
    private JLabeledTextField keyStorePassword = new JLabeledTextField("Key Store Password");
    private JLabeledTextField pathToTrustStore = new JLabeledTextField("Path to Trust Store");
    private JLabeledTextField trustStorePassword = new JLabeledTextField("Trust Store Password");
    private final JCheckBox cbSSL = new JCheckBox("SSL?", false);
    private final JCheckBox cbSSLClientCert = new JCheckBox("Client SSL Cert?", false);
    private JLabeledTextField timeout = new JLabeledTextField("Timeout (milliseconds, 0 = infinite)");
    private JLabeledTextField heartbeat = new JLabeledTextField("Heartbeat (seconds, 0 = none)");

    /**
     * Create a new AMQPConnectionManagerGui as a standalone component.
     */
    public AMQPConnectionManagerGui() {
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStaticLabel() {
        return "AMQP Connection Manager";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (!(element instanceof AMQPConnectionManager)) return;
        AMQPConnectionManager manager = (AMQPConnectionManager) element;

        virtualHost.setText(manager.getVirtualHost());
        host.setText(manager.getHost());
        port.setText(manager.getPort());
        username.setText(manager.getUsername());
        password.setText(manager.getPassword());
        pathToKeyStore.setText(manager.getPathToKeyStore());
        keyStorePassword.setText(manager.getKeyStorePassword());
        pathToTrustStore.setText(manager.getPathToTrustStore());
        trustStorePassword.setText(manager.getTrustStorePassword());
        cbSSL.setSelected(manager.connectionSSL());
        cbSSLClientCert.setSelected(manager.sslClientCert());
        timeout.setText(manager.getTimeout());
        heartbeat.setText(manager.getHeartbeat());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        virtualHost.setText("/");
        host.setText("localhost");
        port.setText(AMQPConnectionManager.DEFAULT_PORT_STRING);
        username.setText("guest");
        password.setText("guest");
        pathToKeyStore.setText("");
        keyStorePassword.setText("");
        pathToTrustStore.setText("");
        trustStorePassword.setText("");
        cbSSL.setSelected(false);
        cbSSLClientCert.setSelected(false);
        timeout.setText(AMQPConnectionManager.DEFAULT_TIMEOUT_STRING);
        heartbeat.setText(AMQPConnectionManager.DEFAULT_HEARTBEAT_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement element) {
        AMQPConnectionManager manager = (AMQPConnectionManager) element;
        manager.clear();
        configureTestElement(manager);

        manager.setVirtualHost(virtualHost.getText());
        manager.setHost(host.getText());
        manager.setPort(port.getText());
        manager.setUsername(username.getText());
        manager.setPassword(password.getText());
        manager.setPathToKeyStore(pathToKeyStore.getText());
        manager.setKeyStorePassword(keyStorePassword.getText());
        manager.setPathToTrustStore(pathToTrustStore.getText());
        manager.setTrustStorePassword(trustStorePassword.getText());
        manager.setConnectionSSL(cbSSL.isSelected());
        manager.setSSLClientCert(cbSSLClientCert.isSelected());
        manager.setTimeout(timeout.getText());
        manager.setHeartbeat(heartbeat.getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        AMQPConnectionManager manager = new AMQPConnectionManager();
        modifyTestElement(manager);
        return manager;
    }

    protected void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title

        JPanel mainPanel = new VerticalPanel();

        mainPanel.add(makeCommonPanel());
        add(mainPanel);
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

        JPanel serverSettings = new JPanel(new GridBagLayout());
        serverSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connection"));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        serverSettings.add(virtualHost, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        serverSettings.add(host, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        serverSettings.add(port, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        serverSettings.add(cbSSL, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        serverSettings.add(cbSSLClientCert, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        serverSettings.add(username, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        serverSettings.add(password, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        serverSettings.add(pathToKeyStore, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        serverSettings.add(keyStorePassword, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        serverSettings.add(pathToTrustStore, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        serverSettings.add(trustStorePassword, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        serverSettings.add(timeout, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        serverSettings.add(heartbeat, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 0;

        commonPanel.add(serverSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
