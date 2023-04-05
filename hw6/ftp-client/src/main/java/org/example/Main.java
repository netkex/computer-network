package org.example;

import org.apache.commons.cli.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        Option server = new Option("s", "server", true, "ftp server name");
        server.setRequired(true);
        options.addOption(server);

        Option port = new Option("p", "port", true, "ftp server port");
        port.setRequired(true);
        options.addOption(port);

        Option login = new Option("lgn", "login", true, "ftp login");
        login.setRequired(true);
        options.addOption(login);

        Option password = new Option("pswrd", "password", true, "ftp password");
        password.setRequired(true);
        options.addOption(password);

        try {
            CommandLine cmd = parser.parse(options, args);

            try(FtpClient ftpClient = new FtpClient(
                    cmd.getOptionValue("server"),
                    Integer.parseInt(cmd.getOptionValue("port")),
                    cmd.getOptionValue("login"),
                    cmd.getOptionValue("password")
            )) {
                Scanner in = new Scanner(System.in);

                program:
                while (true) {
                    final var command =  in.nextLine().split("[ ]+");
                    if (command.length < 1)
                        throw new RuntimeException("incorrect command");
                    switch (command[0]) {
                        case "e":
                            break program;
                        case "l":
                            if (command.length < 2)
                                throw new RuntimeException("incorrect command");
                            final var files = ftpClient.listFiles(command[1]);
                            System.out.println(String.join("\n", files));
                            break;
                        case "d":
                            if (command.length < 3)
                                throw new RuntimeException("incorrect command");
                            ftpClient.downloadFile(command[1], command[2]);
                            break;
                        case "u":
                            if (command.length < 3)
                                throw new RuntimeException("incorrect command");
                            ftpClient.uploadFile(command[1], command[2]);
                            break;
                        default:
                            throw new RuntimeException("unknown command type: " + command[0]);
                    }
                }
            }
        } catch (ParseException e) {
            System.out.println("Arguments exception");
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Clause: " + e.getCause());
        }
    }
}