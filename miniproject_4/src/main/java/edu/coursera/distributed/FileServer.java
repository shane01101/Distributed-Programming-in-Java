package edu.coursera.distributed;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
            final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {
            Socket s = socket.accept();
            
            Thread thread = new Thread(new Runnable() {
                @Override    
                public void run() {
                    try {
                        InputStream input = s.getInputStream();
                        InputStreamReader reader = new InputStreamReader(input);
                        BufferedReader buffered = new BufferedReader(reader);

                        String line = buffered.readLine();
                        assert line != null;
                        assert line.startsWith("GET");
                        final String path = line.split(" ")[1];

                        final PCDPPath fullPath = new PCDPPath(path);

                        OutputStream out = s.getOutputStream();
                        PrintWriter printer = new PrintWriter(out);

                        if(fs.readFile(fullPath) != null) 
                        {
                            printer.write("HTTP/1.0 200 OK\r\n");
                            printer.write("Server: FileServer\r\n");
                            printer.write("\r\n");
                            printer.write(fs.readFile(fullPath) + "\r\n");
                        }
                        else
                        {
                            printer.write("HTTP/1.0 404 Not Found\r\n");
                            printer.write("Server: FileServer\r\n");
                            printer.write("\r\n");
                        }
                        printer.close();
                        s.close();

                    } catch (IOException e) 
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
        }
    }
}
