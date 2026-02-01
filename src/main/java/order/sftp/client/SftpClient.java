package main.java.order.sftp.client;

import java.io.InputStream;

public interface SftpClient {

    void connect(String host, int port, String username, String password);

    void load(String filePath, InputStream InputStream, String fileName);

    void close();
}
