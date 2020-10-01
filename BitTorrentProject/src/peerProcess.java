import java.net.*;
import java.io.*;
import java.nio.charset.*;
// import java.time.Period;
// import java.util.List;
// import java.io.File; 
// import java.io.FileNotFoundException;
// import java.util.Scanner;
// import java.util.ArrayList;

public class peerProcess 
{
    public static void main(String[] args) throws Exception 
    {
        PeerInfo MyPeer = PeerInfo.getPeerInfo(java.net.InetAddress.getLocalHost().toString().split("/")[0]);
		
        try
        {
            if(PeerInfo.MyPeerId != Common.GetSmallestPeerId())
            {
                //connect to all the previous peers
                for (PeerInfo p : Common.getPeerInfo()) 
                {   
                    // StringBuilder s = new StringBuilder();
                    // for( int i = 0; i < Common.getPiece();  i++ )
                    // {
                    //     s.append( p.FileBits.get( i ) == true ? "1" : "0" );
                    //     s.append(" ");
                    // }
                    System.out.println("HostName: " + p.HostName + " Bits: " + s);
                    if (p.PeerId < PeerInfo.MyPeerId) 
                    {   
                        System.out.println("Attempting to connect to client: " + p.PeerId + " " + p.HostName);             
                        Client client = new Client();
                        client.run(p);
                    }
                }
                
            }
            else
                System.out.println("First client running");
        }
        catch (Exception e)
        {
            Log.Write("Error: " + e.getMessage() + System.lineSeparator() + e.getStackTrace());
        }
        
        //open connection for future peers to connect
        ServerSocket listener = new ServerSocket(PeerInfo.MyPortNumber);
        System.out.println("The server is running."); 
        Log.Write("The server is running for: " + PeerInfo.MyHostName);
        try 
        {
            while(true) 
            {
                new Handler(listener.accept(), MyPeer).start();
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
        Charset charset = StandardCharsets.UTF_16;
        byte handshakeheader[] = "P2PFILESHARINGPROJ".getBytes(charset);
        byte handshakezbits[] = "0000000000".getBytes(charset);
        byte handshakepid[];
        private PeerInfo MyPeer;

        public Handler(Socket connection, PeerInfo p) 
        {
            this.connection = connection;
            this.MyPeer = p;
        }

        public void run(PeerInfo MyP) 
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
                            System.out.println("Receive message: " + message + " from peer: " + message.split("0000000000")[1]);
                            if(message.contains("P2PFILESHARINGPROJ"))
                            {
                                handshakepid = Integer.toString(MyPeer.PeerId).getBytes(charset);
                                sendMessage(Common.concat(handshakeheader,handshakezbits,handshakepid));
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
                System.out.println("Disconnect with Client ");
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
                    System.out.println("Disconnect with Client ");
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
                System.out.println("Send message: " + new String(msg, charset));
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        }
    }
}
