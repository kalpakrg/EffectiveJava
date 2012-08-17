package performance.nio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

public final class SimpleEchoServer implements Runnable, EchoServer {
    protected Executor executor;
    protected ServerSocket server;
    protected final int port;
    protected final Thread thread = new Thread(this);
    protected boolean stopped = false;

    public final void start() {
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress("localhost", port));

            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void stop() {
        stopped = true;
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public SimpleEchoServer(Executor executor, int port) {
        this.executor = executor;
        this.port = port;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                Socket client = server.accept();
                executor.execute(new ProtocolHandler(client));
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
        }
    }

    private static final class ProtocolHandler implements Runnable {
        private final Socket socket;
        
        public ProtocolHandler(Socket socket) {
            super();
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                //System.out.println("Handling client at " + socket.getInetAddress().getHostAddress() + ":"  + socket.getPort());
                BufferedReader reader =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String line = reader.readLine();
                while (line != null) {
                    writer.write("Server: " + line + "\n");
                    writer.flush();
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
