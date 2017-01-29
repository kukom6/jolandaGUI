package gui.jolanda;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import static gui.jolanda.utils.WaitUtils.pause;

public class Window extends JDialog {
    private JPanel contentPane;
    private JButton start;
    private JButton shutDown;
    private JButton checkStatusButton;
    private JLabel statusLabel;
    private JTextArea consoleApp;
    private JButton checkTempButton;
    private JTextField commandTextField;
    private JButton runButton;
    private Properties prop;

    private ComputerManager jolanda;


    public Window(Properties prop) {
//        setTitle("Jolanda controller");
        jolanda = new ComputerManager(new Computer("Jolanda", prop.getProperty("ipAddress"),
                prop.getProperty("macAddress"),
                prop.getProperty("username"),
                prop.getProperty("password")));

        this.statusLabel.setOpaque(true);
        DefaultCaret caret = (DefaultCaret) consoleApp.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.consoleApp.setEnabled(false);
        this.consoleApp.setForeground(Color.black);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(start);
        pack();
        setSize(600, 500);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        PrintStream con = new PrintStream(new TextAreaOutputStream(consoleApp));
        System.setOut(con);
        System.setErr(con);

        this.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        while(true) {
                            jolanda.checkStatus();
                            changeStatus(jolanda.isRunning());
                            pause(1000);
                        }
                    }
                };
                worker.start();
            }
        });

        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        lockButton();
                        jolanda.powerOn();
                        // Report the result using invokeLater().
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                refreshButton();
                                setEnabled(true);
                            }
                        });
                    }
                };

                worker.start();
            }
        });

        shutDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        lockButton();
                        jolanda.powerOff();
                        // Report the result using invokeLater().
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                refreshButton();
                                setEnabled(true);
                            }
                        });
                    }
                };

                worker.start();
            }
        });

        checkStatusButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jolanda.checkStatus();
                changeStatus(jolanda.isRunning());
            }
        });

        checkTempButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        checkTempButton.setText("Checking temperature");
                        checkTempButton.setEnabled(false);
                        jolanda.checkTemperature();
                        // Report the result using invokeLater().
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                checkTempButton.setText("CheckTemp");
                                checkTempButton.setEnabled(true);
                            }
                        });
                    }
                };
                worker.start();
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread worker = new Thread(() -> {
                    runButton.setText("Running command");
                    runButton.setEnabled(false);
                    jolanda.executeCommand(commandTextField.getText());
                    // Report the result using invokeLater().
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            runButton.setText("Run");
                            runButton.setEnabled(true);
                        }
                    });
                });
                worker.start();
            }
        });
    }

    /**
     * Change status in notification
     */
    private synchronized void changeStatus(boolean isRunning) {
        if (isRunning) {
            this.statusLabel.setText("Jolanda is running");
            this.statusLabel.setBackground(Color.GREEN);
        } else {
            this.statusLabel.setText("Jolanda is not running");
            this.statusLabel.setBackground(Color.RED);
        }
    }

    /**
     * Enable and disable buttons
     */
    private void refreshButton() {
        if (jolanda.isRunning()) {
            start.setEnabled(false);
            shutDown.setEnabled(true);
            checkTempButton.setEnabled(true);
            checkStatusButton.setEnabled(true);
        } else {
            start.setEnabled(true);
            shutDown.setEnabled(false);
            checkTempButton.setEnabled(false);
            checkStatusButton.setEnabled(true);
        }
    }

    /**
     * Lock button when thread is running
     */
    private void lockButton() {
        start.setEnabled(false);
        shutDown.setEnabled(false);
        checkTempButton.setEnabled(false);
        checkStatusButton.setEnabled(false);
    }


    /**
     * Help code for console in application
     */
    public class TextAreaOutputStream extends OutputStream {
        private JTextArea textControl;

        /**
         * Creates a new instance of TextAreaOutputStream which writes
         * to the specified instance of javax.swing.JTextArea control.
         *
         * @param control A reference to the javax.swing.JTextArea
         *                control to which the output must be redirected
         *                to.
         */
        public TextAreaOutputStream(JTextArea control) {
            textControl = control;
        }

        /**
         * Writes the specified byte as a character to the
         * javax.swing.JTextArea.
         *
         * @param b The byte to be written as character to the
         *          JTextArea.
         */
        public void write(int b) throws IOException {
            // append the data as characters to the JTextArea control
            textControl.append(String.valueOf((char) b));
        }
    }
}