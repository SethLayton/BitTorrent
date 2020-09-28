import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class Client 
{
	Socket requestSocket;           //socket connect to the server
	DataOutputStream out;         //stream write to the socket
 	DataInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	Charset charset = StandardCharsets.UTF_16;
	byte handshake[] = "P2PFILESHARINGPROJ00000000000001".getBytes(charset);
	

	public void Client() {}

	void run()
	{
        try
        {
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new DataOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new DataInputStream(requestSocket.getInputStream());

			//System.out.println("MESSAGE= " + MESSAGE);
			while(true)
			{
				if(MESSAGE == null || !MESSAGE.equals("OK"))
				{
					//System.out.println(new String(handshake, charset));
					sendMessage(handshake);
					byte[] recdata;
					int length = in.readInt();
					if(length>0) 
					{
						recdata = new byte[length];
						in.readFully(recdata, 0, recdata.length);
						MESSAGE = new String(recdata, charset);
					}
					//System.out.println("Receive message: " + MESSAGE);
					if (MESSAGE.equals("OK"))
						System.out.println("Connection Established");
				}
				else
				{					
					//Do some file transfer stuff here
				}				
			}	
		}
        catch (ConnectException e) 
        {
            System.err.println("Connection refused. You need to initiate a server first.");
		} 
        catch (Exception e ) 
        {
                System.err.println("Error: " + e.getMessage() + "\n" + e.getStackTrace());
        } 
        finally
        {
			//Close connections
            try
            {
				in.close();
				out.close();
				requestSocket.close();
			}
            catch(IOException ioException)
            {
				ioException.printStackTrace();
			}
		}
	}
	
	//send a message to the output stream
	void sendMessage(byte[] msg)
	{
        try
        {
			//stream write the message
			out.writeInt(msg.length);
			out.write(msg);
			out.flush();
		}
        catch(IOException ioException)
        {
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}
}
