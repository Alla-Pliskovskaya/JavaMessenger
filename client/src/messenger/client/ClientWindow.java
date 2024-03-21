package messenger.client;

import messenger.network.TCPConnection;
import messenger.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 8189;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 550;
    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new HintTextField("Name");
    private final JTextField fieldInput = new HintTextField("Message");
    private TCPConnection connection;

    public ClientWindow()
    {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                connection.sendString(fieldNickname.getText() + " left the chat");
            }
        });

        setTitle("Java messenger");
        setBackground(new Color(75, 0, 130));
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true); // при передаче значения true окно будет всегда располагаться поверх других окон

        log.setEditable(false);
        log.setLineWrap(true); // для длинных строк выполняется перенос
        log.setBackground(new Color(176, 196, 222));
        log.setFont(new Font("System", Font.BOLD, 14));
        add(log, BorderLayout.CENTER);

        fieldInput.setBackground(new Color(135, 206, 250));
        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);

        fieldNickname.setHorizontalAlignment(JTextField.CENTER);
        fieldNickname.setBackground(new Color(135, 206, 250));
        fieldNickname.addActionListener(e -> {
            if (fieldNickname.getText().isEmpty())
                fieldNickname.setText("Anonymous");
            connection.sendString("Client is " + fieldNickname.getText());
        });
        add(fieldNickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDRESS, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientWindow::new); // () -> new ClientWindow()
//       SwingUtilities.invokeLater(new Runnable() {
//           @Override
//           public void run() {
//               new ClientWindow();
//           }
//       });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.isEmpty()) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection is ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection closed...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception: " + e);
    }

    private synchronized void printMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }
}
