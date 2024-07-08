import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    static ServerSocket serverSocket = null;
    static Socket clientSocket = null;
    static int port = 6379;
    static PrintWriter out = null;
    static BufferedReader in = null;
    static String CRLF = "\r\n";

  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
        try {
          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);
         
          // Wait for connection from client.
          clientSocket = serverSocket.accept();

          // Intialize The output and input connections to the socket
          out = new PrintWriter(clientSocket.getOutputStream(), true);
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

          String input;

          while( (input = in.readLine()) != null) {
            System.out.println(input);
            if(input.equalsIgnoreCase("PING")) {
              out.write("+PONG" + CRLF);
              out.flush();
            }
          } 

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
       } finally {
         try {
           stop();
         } catch (IOException e) {
           System.out.println("IOException: " + e.getMessage());
         }
       }
  }

  public static void stop() throws IOException {
    if(clientSocket  != null) {
      clientSocket.close();;
    }
    in.close();
    out.close();
    serverSocket.close();
  }
}
