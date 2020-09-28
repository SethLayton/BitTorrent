import java.net.*;
import java.io.*;
import java.nio.charset.*;

public class Server 
{

    
	private static final int sPort = 8000;   //The server will be listening on this port number

    public static void main(String[] args) throws Exception 
    {
		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try 
        {
            while(true) 
            {
                new Handler(listener.accept(),clientNum).start();
                System.out.println("Client "  + clientNum + " is connected!");
                clientNum++;
            }
        } 
        finally 
        {
                listener.close();
        } 
 
    }

	/**
    * A handler thread class.  Handlers are spawned from the listening
    * loop and are responsible for dealing with a single client's requests.
    */
    private static class Handler extends Thread 
    {
        private String message;    //message received from the client
        private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private int no;		//The index number of the client
        Charset charset = StandardCharsets.UTF_16;

        public Handler(Socket connection, int no) 
        {
            this.connection = connection;
            this.no = no;
        }

        public void run() 
        {
            try
            {
                //initialize Input and Output streams
                out = new DataOutputStream(connection.getOutputStream());
                out.flush();
                in = new DataInputStream(connection.getInputStream());
                try
                {
                    while(true)
                    {
                        //receive the message sent from the client
                        int length = in.readInt();
                        byte[] data;
                        if(length>0) 
                        {
                            data = new byte[length];
                            in.readFully(data, 0, data.length);
                            message = new String(data, charset);
                             //show the message to the user
                            System.out.println("Receive message: " + message + " from client " + no);
                            if(message.contains("P2PFILESHARINGPROJ"))
                            {
                                sendMessage("OK".getBytes(charset));
                            }
                            else
                            {
                                sendMessage(message.toUpperCase().getBytes(charset));
                            }                            
                        }
                        else
                        {
                            sendMessage("Handshake of length 0 received".getBytes(charset));
                        }
                        
                       
                    }
                }
                catch(Exception e)
                {
                    System.err.println("Error: " + e.getMessage() + "\n");
                }
            }
            catch(IOException ioException)
            {
                System.out.println("Disconnect with Client " + no);
            }
            finally
            {
                //Close connections
                try
                {
                    in.close();
                    out.close();
                    connection.close();
                }
                catch(IOException ioException)
                {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }
        
       
        //send a message to the output stream
        public void sendMessage(byte[] msg)
        {
            try
            {
                out.writeInt(msg.length);
                out.write(msg);
                out.flush();
                System.out.println("Send message: " + new String(msg, charset) + " to Client " + no);
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        }
    }
}
