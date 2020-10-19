import java.net.*;
import java.io.*;
import java.nio.charset.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.BitSet;

public class Client 
{	
	public static class Handler extends Thread 
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
		private PeerInfo connectedPeer;
	
		public Handler(PeerInfo p) 
        {
            this.connectedPeer = p;
        }

		public void run()
		{
			try
			{	
				handshakepid = Integer.toString(PeerInfo.MyPeerId).getBytes(charset);
				//System.out.println("Connecting to " + connectedPeer.HostName + ":" + connectedPeer.PortNumber);
				//create a socket to connect to the server
				requestSocket = new Socket(connectedPeer.HostName, connectedPeer.PortNumber);
				System.out.println(MessageFormat.format("Connected to {0} on port: {1,number,#}", connectedPeer.HostName, connectedPeer.PortNumber));
				//initialize inputStream and outputStream
				out = new DataOutputStream(requestSocket.getOutputStream());
				out.flush();
				in = new DataInputStream(requestSocket.getInputStream());

				while(true)
				{
					if((MESSAGE == null || !MESSAGE.contains(String.valueOf(connectedPeer.PeerId)) && !PeerInfo.isHandShake(connectedPeer.PeerId)))
					{
						//System.out.println(new String(Common.concat(handshakeheader,handshakezbits,handshakepid), charset));
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
						if (MESSAGE.contains(String.valueOf(connectedPeer.PeerId)))
						{
							PeerInfo.SetHandshake(connectedPeer.PeerId, true);
							Log.Write(MessageFormat.format(" Peer {0} makes a connection to Peer {1,number,#}", PeerInfo.MyPeerId, connectedPeer.PeerId));
							System.out.println("Handshake complete with peer: " + connectedPeer.PeerId + ". Connection established");
                            //Send the bitfield message first
							System.out.println("Attempting to send the bitarray");
							StringBuilder s = new StringBuilder();
							for( int i = 0; i < Common.Piece;  i++ )
							{
								s.append( PeerInfo.MyFileBits.get(i) == true ? "1" : "0" );
								s.append(" ");
							}
							System.out.println(s);
                            sendMessage(PeerInfo.MyFileBits.toByteArray());
						}
                    }
                    else
                    {
                        byte[] recdata;
						int length = in.readInt();
						if(length>0) 
						{
							recdata = new byte[length];
							in.readFully(recdata, 0, recdata.length);
						
                            //Do some file transfer stuff here
                            //we expect a bitfield back from the connected peer first
                            connectedPeer.FileBits = BitSet.valueOf(recdata);
                            System.out.println("Receive message (bitfield) from peer: " + connectedPeer.PeerId);
                            StringBuilder s = new StringBuilder();
                            for( int i = 0; i < Common.Piece;  i++ )
                            {
                                s.append( connectedPeer.FileBits.get(i) == true ? "1" : "0" );
                                s.append(" ");
                            }
                            System.out.println(s);
                        }
                    }				
				}	
			}
			catch (IOException e) 
			{
				System.err.println("Connection closed with: " + connectedPeer.HostName);
				PeerInfo.SetHandshake(connectedPeer.PeerId, false);
			}
			catch (Exception e ) 
			{
                    System.err.println("Error: " + e.getMessage() + "\n");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString(); // stack trace as a string
                    System.out.println(sStackTrace);
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
}
