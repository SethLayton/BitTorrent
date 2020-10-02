import java.net.*;
import java.io.*;
import java.nio.charset.*;
import java.text.MessageFormat;

public class Client 
{
	Socket requestSocket;          //socket connect to the server
	DataOutputStream out;          //stream write to the socket
 	DataInputStream in;            //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	Charset charset = StandardCharsets.UTF_16;
	byte handshakeheader[] = "P2PFILESHARINGPROJ".getBytes(charset);
	byte handshakezbits[] = "0000000000".getBytes(charset);
	byte handshakepid[];
	
	//public void Client(){}
	
	void run(PeerInfo p)
	{
        try
        {	
			handshakepid = Integer.toString(p.PeerId).getBytes(charset);
			System.out.println("Connecting to " + p.HostName + ":" + p.PortNumber);
			//create a socket to connect to the server
			requestSocket = new Socket(p.HostName, p.PortNumber);
			System.out.println(MessageFormat.format("Connected to {0} on port: {1}", p.HostName, p.PortNumber));
			//initialize inputStream and outputStream
			out = new DataOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new DataInputStream(requestSocket.getInputStream());

			//System.out.println("MESSAGE= " + MESSAGE);
			while(true)
			{
				if(MESSAGE == null || !MESSAGE.contains(String.valueOf(p.PeerId)))
				{
					//System.out.println(new String(handshake, charset));
					sendMessage(Common.concat(handshakeheader,handshakezbits,handshakepid));
					byte[] recdata;
					int length = in.readInt();
					if(length>0) 
					{
						recdata = new byte[length];
						in.readFully(recdata, 0, recdata.length);
						MESSAGE = new String(recdata, charset);
					}
					//System.out.println("Receive message: " + MESSAGE);
					if (MESSAGE.contains(String.valueOf(p.PeerId)))
						System.out.println("Handshake complete with peer: " + p.PeerId + ". Connection established");
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
}
