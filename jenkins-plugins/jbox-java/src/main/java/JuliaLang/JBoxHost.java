package JuliaLang;

import com.jcraft.jsch.*;
import java.io.*;
import java.util.logging.Logger;

/**
 *
 * @author Keno Fischer
 */
public class JBoxHost {

    public String hostname;
    public int port;
    public String username;
    public String password;
    public String keyfile;
    public static final Logger LOGGER = Logger.getLogger(JBoxHost.class.getName());

    public JBoxHost(String hostname, String port, String username, String password) {
        this.hostname = hostname;
        try {
            this.port = Integer.parseInt(port);
        } catch (Exception e) {
            this.port = 22;
        }
        this.username = username;
        this.password = password;
    }

    public JBoxHost(String hostname, String port, String username, String passphrase, String keyfile) {
        this(hostname, port, username, passphrase);

        this.keyfile = keyfile;
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(username, hostname, port);
        if (this.keyfile != null && this.keyfile.length() > 0) {
            jsch.addIdentity(this.keyfile, this.password);
        } else {
            session.setPassword(password);
        }

        UserInfo ui = new JBoxUserInfo(password);
        session.setUserInfo(ui);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        return session;
    }

    public int executeCommand(String command, OutputStream writer) {
        Session session = null;
        ChannelExec channel = null;
        int status = -1;
        try {
            session = createSession();
            channel = createChannel(session);
            channel.setCommand(command);
            channel.setErrStream(writer);
            channel.setOutputStream(writer);
            channel.setInputStream(null);
            channel.connect();

            while (!channel.isClosed()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            status = channel.getExitStatus();

        } catch (JSchException jse) {

            LOGGER.warning("Jsch failure during running " + command);

        } finally {

            closeSession(session, channel);

        }
        return status;
    }

    public int executeCommand(String command, Writer out) {
        Session session = null;
        ChannelExec channel = null;
        int status = -1;
        try {
            session = createSession();
            channel = createChannel(session);
            channel.setCommand(command);
            channel.setErrStream(System.err);
            channel.setInputStream(null);
            InputStream is = channel.getInputStream();
            channel.connect();

            if (out != null) {


                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String line;

                while ((line = in.readLine()) != null
                        || !channel.isClosed()) {

                    if (line != null) {

                        out.write(line + '\n');

                        out.flush();

                    }

                }

                in.close();

            } else {
                byte[] tmp = new byte[1024];
                while (true) {
                    while (is.available() > 0) {
                        int i = is.read(tmp, 0, 1024);
                        if (i < 0) {
                            break;
                        }
                        System.out.print(new String(tmp, 0, i));
                    }
                    if (channel.isClosed()) {
                        System.out.println("exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                    }
                }
            }

            is.close();

            status = channel.getExitStatus();

        } catch (JSchException jse) {

            LOGGER.warning("Jsch failure during running " + command);

        } catch (IOException ex) {

            LOGGER.warning("IO failure during running " + command);

        } finally {

            closeSession(session, channel);

        }
        return status;
    }

    public void testConnection() throws JSchException, IOException {
        Session session = createSession();
        closeSession(session, null);
    }

    private ChannelExec createChannel(Session session) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        return channel;
    }

    private void closeSession(Session session, ChannelExec channel) {
        if (channel != null) {
            channel.disconnect();
            channel = null;
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }
}
