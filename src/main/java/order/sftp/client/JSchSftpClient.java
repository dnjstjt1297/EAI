package main.java.order.sftp.client;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.Optional;
import main.java.global.exception.RestApiException;
import main.java.global.exception.errorcode.enums.OrderErrorCode;
import main.java.global.logging.annotation.LogExecution;

public class JSchSftpClient implements SftpClient {

    private static final String CONFIG_KEY = "StrictHostKeyChecking";
    private static final String CONFIG_VALUE = "no";
    private static final String CHANNEL_TYPE = "sftp";

    private Session session;
    private Channel channel;

    @Override
    @LogExecution
    public void connect(String host, int port, String username, String password) {
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig(CONFIG_KEY, CONFIG_VALUE);
            session.connect();

            channel = session.openChannel(CHANNEL_TYPE);
            channel.connect();

        } catch (JSchException e) {
            throw new RestApiException(OrderErrorCode.FAILED_SFTP_CONNECT);

        }

    }

    @Override
    @LogExecution
    public void load(String filePath, InputStream inputStream, String fileName) {
        ChannelSftp channelSftp = (ChannelSftp) channel;
        try {
            channelSftp.cd(filePath);
            channelSftp.put(inputStream, fileName);

        } catch (Exception e) {
            close();
            throw new RestApiException(OrderErrorCode.FAILED_SFTP_LOAD);
        }
    }

    @Override
    @LogExecution
    public void close() {
        Optional.ofNullable(channel)
                .filter(Channel::isConnected)
                .ifPresent(Channel::disconnect);

        Optional.ofNullable(session)
                .filter(Session::isConnected)
                .ifPresent(Session::disconnect);
    }

}
