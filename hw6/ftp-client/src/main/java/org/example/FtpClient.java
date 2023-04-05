package org.example;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FtpClient implements AutoCloseable {
    private final FTPClient ftpHandler;

    public FtpClient(String server, int port, String user, String password) throws IOException {
        ftpHandler = new FTPClient();
        ftpHandler.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftpHandler.connect(server, port);
        if (!FTPReply.isPositiveCompletion(ftpHandler.getReplyCode())) {
            ftpHandler.disconnect();
            throw new IOException("failed to connect to ftp server");
        }

        if (!ftpHandler.login(user, password)) {
            ftpHandler.disconnect();
            throw new IOException("failed to login in ftp Server");
        }
    }

    public List<String> listFiles(String path) throws IOException {
        FTPFile[] files = ftpHandler.listFiles(path);
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());
    }

    public void downloadFile(String source, String destination) throws IOException {
        FileOutputStream out = new FileOutputStream(destination);
        ftpHandler.retrieveFile(source, out);
        out.close();
    }

    public void uploadFile(String source, String destination) throws IOException {
        FileInputStream in = new FileInputStream(source);
        ftpHandler.storeFile(destination, in);
        in.close();
    }

    public void close() throws IOException {
        ftpHandler.disconnect();
    }
}
