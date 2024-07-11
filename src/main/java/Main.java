import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
        System.out.println("Client Connected: " + clientSocket);
        StringBuilder clientRequest = new StringBuilder();
        String input;
        try {
          while((input = in.readLine()) != null) {
                clientRequest.append(input);
                clientRequest.append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        
        System.out.println("Client Request:" + clientRequest);
        List<String> tokens = deserializeClientRequest(clientRequest.toString());
        int itr = 0;

        while(itr < tokens.size()) {
          if(tokens.get(itr).equalsIgnoreCase("PING")) {
            respondSimpleString("PONG");
            itr++;  
          }
          else if(tokens.get(itr).equals("ECHO")){
            respondBulkString(tokens.get(itr + 1));
            itr += 2;
          }
          else{
            System.out.println("Command Not Found: " + tokens.get(itr++));
          }
        }
  
        deleteConnnection();
      }

      public List<String> deserializeClientRequest(String request) {
        int i = 0;
        List<String> tokens = new ArrayList<>();
        while(i < request.length()) {
          char firstCharacter = request.charAt(i);
          i++;
          switch (firstCharacter) {
              case '*':
                  i = processArrayType(i, request, tokens);
                  break;
              case '+':
                  i = processSimpleStringType(i+1, request, tokens);
              default:
                  throw new AssertionError();
          }
        }

        return tokens;
      }

      int processArrayType(int start, String request, List<String> tokens) {

        return  0;
      }

      int processSimpleStringType(int start, String request, List<String> tokens) {
          int endIndex = request.indexOf(CRLF, start);
          String simple = request.substring(start , endIndex);
          tokens.add(simple);
          return endIndex + CRLF.length() + 1;
      }

      void respondSimpleString(String simple) {
        out.write("+" + simple + CRLF);
        out.flush();
      }

      void respondBulkString(String outpuString) {
        
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
    System.out.println("Logs from your program will appear here!");
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
