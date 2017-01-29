import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Window extends JDialog {
    private JPanel contentPane;
    private JButton start;
    private JButton shutDown;
    private JButton checkStatusButton;
    private JLabel statusLabel;
    private JTextArea consoleApp;
    private JButton checkTempButton;
    private Properties prop;

    private PcManager pcManager;



    public Window(Properties prop) {
//        setTitle("Jolanda controller");
        pcManager = new PcManager(prop.getProperty("ipAddress"),
                prop.getProperty("macAddress"),
                prop.getProperty("username"),
                prop.getProperty("password"));

        this.statusLabel.setOpaque(true);
        DefaultCaret caret = (DefaultCaret)consoleApp.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.consoleApp.setEnabled(false);
        this.consoleApp.setForeground(Color.black);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(start);
        pack();
        setSize(600,500);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        PrintStream con=new PrintStream(new TextAreaOutputStream(consoleApp));
        System.setOut(con);
        System.setErr(con);

        this.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        lockButton();
                        statusLabel.setText("Checking status. Please wait");
                        statusLabel.setBackground(Color.orange);
                        if(pcManager.isRunning()){
                            changeStatus(true);
                        }else{
                            changeStatus(false);
                        }
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

        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        lockButton();
                        startPC();
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
                        shutDownPC();
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
                Thread worker = new Thread() {
                    public void run() {
                        statusLabel.setText("Checking status. Please wait");
                        statusLabel.setBackground(Color.orange);
                        checkStatusButton.setEnabled(false);
                        changeStatus(pcManager.isRunning());
                        // Report the result using invokeLater().
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                checkStatusButton.setEnabled(true);
                            }
                        });
                    }
                };
                worker.start();
            }
        });

        checkTempButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread worker = new Thread() {
                    public void run() {
                        checkTempButton.setText("Checking temperature");
                        checkTempButton.setEnabled(false);
                        checkTemp();
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
    }

    /**
     * Start jolanda
     */
    private void startPC() {
        if(pcManager.isRunning()){
            System.out.println("Jolanda is already running");
            return;
        }
        this.statusLabel.setText("Jolanda will be started. Please wait");
        this.statusLabel.setBackground(Color.orange);
        pcManager.powerOn();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!pcManager.isRunning()){
            this.statusLabel.setText("Jolanda wasn't been started in 60 second. Check status manually");
        }else{
            changeStatus(true);
        }
    }

    /**
     * Shutdown jolanda
     */
    private void shutDownPC() {
        if(!pcManager.isRunning()){
            System.out.println("Jolanda is already offline");
            return;
        }
        this.statusLabel.setText("Jolanda will be shutdown. Please wait");
        this.statusLabel.setBackground(Color.ORANGE);
        pcManager.powerOff();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        changeStatus(false);
    }

    /**
     * Change status in notification
     */
    private void changeStatus(boolean isRunning){
        if(isRunning){
            this.statusLabel.setText("Jolanda is running");
            this.statusLabel.setBackground(Color.GREEN);
        }else{
            this.statusLabel.setText("Jolanda is not running");
            this.statusLabel.setBackground(Color.RED);
        }
    }

    /**
     * Check jolanda temp
     */
    private void checkTemp(){
        pcManager.checkTemperature();
    }

    /**
     * Enable and disable buttons
     */
    private void refreshButton(){
        if(pcManager.isRunning()){
            start.setEnabled(false);
            shutDown.setEnabled(true);
            checkTempButton.setEnabled(true);
            checkStatusButton.setEnabled(true);
        }else{
            start.setEnabled(true);
            shutDown.setEnabled(false);
            checkTempButton.setEnabled(false);
            checkStatusButton.setEnabled(true);
        }
    }

    /**
     * Lock button when thread is running
     */
    private void lockButton(){
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
         * @param control   A reference to the javax.swing.JTextArea
         *                  control to which the output must be redirected
         *                  to.
         */
        public TextAreaOutputStream( JTextArea control ) {
            textControl = control;
        }

        /**
         * Writes the specified byte as a character to the
         * javax.swing.JTextArea.
         *
         * @param   b   The byte to be written as character to the
         *              JTextArea.
         */
        public void write( int b ) throws IOException {
            // append the data as characters to the JTextArea control
            textControl.append( String.valueOf( ( char )b ) );
        }
    }
}