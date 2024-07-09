import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main{
    static ServerSocket serverSocket = null;
    static int port = 6379;
    static String CRLF = "\r\n";


    static class Client extends Thread {
      Socket clientSocket;
      PrintWriter out;
      BufferedReader in;

      public Client(Socket clientSocket) throws Exception {
        this.clientSocket = clientSocket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
      }

      @Override
      public void run(){
        String input;
        try {
          while((input = in.readLine()) != null) {
            System.out.println(input);
            if(input.equalsIgnoreCase("PING")) {
              out.write("+PONG" + CRLF);
              out.flush();
              Thread.sleep(10);
            }
          }
        } catch (IOException | InterruptedException e) {
          System.err.println(e);
        }
  
        deleteConnnection();
      }

      public void deleteConnnection() {
        
        try {
          in.close();
        } catch (IOException e) {
          System.err.println(e);
        }

        out.checkError();
      }
    }

  public static void main(String[] args) throws Exception {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
        try {
          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);

          while(true) {
          // Wait for connection from client.
            Socket clientSocket = serverSocket.accept();
            new Client(clientSocket).start();
          }
           
        } catch (Exception e) {
          System.out.println("IOException: " + e.getMessage());
       } 
  }
}
