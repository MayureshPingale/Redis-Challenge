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
    static String CRLF = "\\r\\n";

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
        String input; //= in.lines().collect(Collectors.joining(System.lineSeparator());
        try {
          while((input = in.readLine()) != null) {
                clientRequest.append(input);
                clientRequest.append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        
        System.out.println(String.format("Client Request: %s. Length: %s", clientRequest, clientRequest.length()));
        List<String> tokens = deserializeClientRequest(clientRequest.toString());
        System.out.println("Extracted tokens: " + tokens);
        int itr = 0;

        while(itr < tokens.size()) {
          if(tokens.get(itr).equalsIgnoreCase("PING")) {
            System.out.println("Responding to PING Command");
            respondSimpleString("PONG");
            itr++;  
          }
          else if(tokens.get(itr).equalsIgnoreCase("ECHO")){
            System.out.println("Responding to ECHO Command");
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
          i = process(i, request, tokens);
          System.out.println(i);
        }

        return tokens;
      }

      int process(int i, String request, List<String> tokens) {
          char firstCharacter = request.charAt(i);
          i++;

          switch (firstCharacter) {
            case '*':
                return processArrayType(i, request, tokens);
            case '+':
                return processSimpleStringType(i, request, tokens);
            case '$':
                return processBulkStringType(i, request, tokens);
            default:
                throw new AssertionError();
        }
      }

      int processArrayType(int start, String request, List<String> tokens) {
        System.out.println(String.format("Array Processing: startIndex: %s request: %s", start, request.substring(start)));
        int firstCRLFIndex = request.indexOf(CRLF, start);
        int totalElements = Integer.parseInt(request.substring(start, firstCRLFIndex));
        System.out.println("Total Ele in array: " + totalElements);
        int itr = firstCRLFIndex + CRLF.length();

        for(int i =0; i < totalElements; i++) {
            itr = process(itr, request, tokens);
        }

        return itr;
      }

      int processSimpleStringType(int start, String request, List<String> tokens) {
          System.out.println(String.format("Simple String Processing: startIndex: %s request: %s", start, request.substring(start)));
          int endIndex = request.indexOf(CRLF, start);
          String simple = request.substring(start , endIndex);
          System.out.println(simple);
          tokens.add(simple);
          return endIndex + CRLF.length();
      }

      int processBulkStringType(int start, String request, List<String> tokens) {
        System.out.println(String.format("Bulk String Processing: startIndex: %s request: %s", start, request.substring(start)));
        int firstCRLFIndex = request.indexOf(CRLF, start);
        int bulkStrLength = Integer.parseInt(request.substring(start, firstCRLFIndex));
        String bulkString = request.substring(firstCRLFIndex +  CRLF.length() , firstCRLFIndex +  CRLF.length() + bulkStrLength);
        System.out.println(bulkString);
        tokens.add(bulkString);
        return firstCRLFIndex +  CRLF.length() + bulkStrLength + CRLF.length();
    }

      void respondSimpleString(String simple) {
        String response = "+" + simple + CRLF;
        System.out.println(response);
        out.write(response);
        out.flush();
      }

      void respondBulkString(String outpuString) {
        String response = "$" + outpuString.length() + CRLF + outpuString + CRLF;
        System.out.println(response);
        out.write(response);
        out.flush();
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
