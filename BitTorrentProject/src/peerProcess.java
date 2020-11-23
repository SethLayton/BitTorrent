import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.*;

import java.util.ArrayList;
import java.util.Arrays;

public class peerProcess 
{
    static BlockingQueue<int[]> queue = new LinkedBlockingQueue<int[]>();
    public static void main(String[] args) throws Exception 
    {
        PeerInfo MyPeer = PeerInfo.getPeerInfo(java.net.InetAddress.getLocalHost().toString().split("/")[0]);
        
        try
        {
            if(PeerInfo.MyPeerId != Common.GetSmallestPeerId())
            {
                //connect to all the previous peers
                for (PeerInfo p : Common.getPeers()) 
                {   
                    if (p.PeerId < PeerInfo.MyPeerId) 
                    {   
                        System.out.println("Attempting to connect to client: " + p.PeerId + " " + p.HostName);   
                        new Handler(p, MyPeer).start();
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
        
        //open connection for future peers to connect if its not the last to start
        if (PeerInfo.MyPeerId != Common.GetLargestPeerId())
        {
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
        ChokingInterval choke = new ChokingInterval(Common.NumberOfPreferredNeighbors, Common.Peers.size(), Common.UnchokingInterval, Common.OptimisticUnchokingInterval);
 
    }
    
	/**
    * A handler thread class.  Handlers are spawned from the listening
    * loop and are responsible for dealing with a single client's requests.
    */
    private static class Handler extends Thread 
    {
        //Socket requestSocket; 
        private String message;    //message received from the client
        private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        //Charset charset = StandardCharsets.UTF_16;
        //byte handshakeheader[] = "P2PFILESHARINGPROJ".getBytes(charset);
        //byte handshakezbits[] = "0000000000".getBytes(charset);
        byte handshakepid[];
        private PeerInfo MyPeer;
        private PeerInfo connectedPeer;
        Boolean client = false;
        Boolean bitSent = false;

        public Handler(Socket connection, PeerInfo p) 
        {
            this.connection = connection;
            this.MyPeer = p;
        }

        public Handler( PeerInfo p, PeerInfo myp) 
        {
            try
			{
                this.MyPeer = myp;
                this.connectedPeer = p;
                connection = new Socket(connectedPeer.HostName, connectedPeer.PortNumber);
                client = true;
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
                        int[] msg = queue.peek();
                        if (msg != null && msg[0] == connectedPeer.PeerId) 
                        {   
                            msg = queue.poll();
                            sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(msg[0]).array()));
                        }
                        if (client && (connectedPeer == null || !PeerInfo.isHandShake(connectedPeer.PeerId)))
                        {
                            handshakepid = Integer.toString(MyPeer.PeerId).getBytes();
                            sendMessage(Common.concat(Message.handshakeheader,Message.handshakezbits,handshakepid));
                        }
                        //receive the message sent from the client
                        int length = in.readInt();
                        byte[] data;
                        if(length>0) 
                        {                        
                            data = new byte[length];
                            in.readFully(data, 0, data.length);
                            message = new String(data);
                            if(message.contains("P2PFILESHARINGPROJ") && (connectedPeer == null || !PeerInfo.isHandShake(connectedPeer.PeerId)))
                            {
                                //Potential need to change here as splitting could give null exception
                                connectedPeer = PeerInfo.getPeerInfo(Integer.parseInt(Common.removeBadFormat(message.split("0000000000")[1])));
                                Log.Write(MessageFormat.format("Peer {0} is connected from Peer {1}", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                handshakepid = Integer.toString(MyPeer.PeerId).getBytes();
                                sendMessage(Common.concat(Message.handshakeheader,Message.handshakezbits,handshakepid));
                                PeerInfo.SetHandshake(connectedPeer.PeerId, true);
                                for (PeerInfo.Pair<Integer, Boolean> b : PeerInfo.hShakeArray) 
                                {
                                    if (b.getLeft() != MyPeer.PeerId)
                                        System.out.println("PeerId: " + b.getLeft() + " isHandshake: " + b.getRight());
                                }
                            }
                            else
                            {  
                                if (!bitSent)
                                {
                                    byte[] temp = Message.createBitfield(PeerInfo.MyFileBits);
                                    sendMessage(temp);
                                    StringBuilder ss = new StringBuilder();
                                    for( int i = 0; i < Common.Piece;  i++ )
                                    {
                                        ss.append( PeerInfo.MyFileBits.get(i) == true ? "1" : "0" );
                                        ss.append(" ");
                                    }
                                    System.out.println("MyFileBits " + ss);
                                    bitSent = true;
                                }
                                ArrayList<byte[]> fields = Message.parseMessage(data);

                                switch (fields.get(1)[0])
                                {
                                    case 0: //choke

                                        break;

                                    case 1: //unchoke

                                        break;

                                    case 2: //interested
                                        connectedPeer.interested = true;
                                        break;

                                    case 3: //not interested
                                        connectedPeer.interested = false;
                                        break;

                                    case 4: //have
                                        ByteBuffer bb = ByteBuffer.wrap(fields.get(2));
                                        int temp1 = bb.getInt();
                                        if (!PeerInfo.MyFileBits.get(temp1))
                                        {
                                            sendMessage(Message.createInterested());
                                        }
                                        connectedPeer.FileBits.set(temp1);
                                        break;

                                    case 5: //bitfield
                                        connectedPeer.FileBits = BitSet.valueOf(fields.get(2));
                                        if (!client)
                                        {
                                            byte[] temp = Message.createBitfield(PeerInfo.MyFileBits);
                                            sendMessage(temp);
                                        }
                                        else
                                        {
                                            BitSet temp = PeerInfo.MyFileBits;
                                            BitSet temp2 = connectedPeer.FileBits;
                                            temp.xor(temp2);                                            
                                            temp2.and(temp);
                                            if (!temp2.isEmpty())
                                            {
                                                sendMessage(Message.createInterested());
                                            }
                                            else
                                                sendMessage(Message.createNotInterested());

                                        }
                                        break;

                                    case 6: //request
                                        ByteBuffer rbb = ByteBuffer.wrap(fields.get(2));
                                        int rindex = rbb.getInt();
                                        int rstart = rindex * Common.PieceSize;
                                        byte[] filechunk = Arrays.copyOfRange(PeerInfo.MyFile, rstart, rstart + Common.PieceSize);
                                        sendMessage(Message.createPiece(Common.concat(fields.get(2), filechunk)));
                                        break;

                                    case 7: //piece
                                        ByteBuffer pbb = ByteBuffer.wrap(Arrays.copyOfRange(fields.get(2),0,4));
                                        int pindex = pbb.getInt();
                                        System.arraycopy(fields.get(2), 4, PeerInfo.MyFile, pindex, Common.PieceSize);

                                        //TODO: Handle sending the 'have' message to all other peers.
                                        for (PeerInfo pi : Common.Peers) 
                                        {
                                            if (pi.PeerId != PeerInfo.MyPeerId)
                                            {
                                                queue.put(new int[] {pi.PeerId, pindex});
                                            }
                                        }
                                        break;
                                }

                                // //Do some filetransfer stuff here
                                // //Handshake has been successfully created with this peer
                                // //Expect to see the bitfield message here first
                                // connectedPeer.FileBits = BitSet.valueOf(data);
                                // System.out.println("Receive message (bitfield) from peer: " + connectedPeer.PeerId);
                                // StringBuilder s = new StringBuilder();
                                // for( int i = 0; i < Common.Piece;  i++ )
                                // {
                                //     s.append( connectedPeer.FileBits.get(i) == true ? "1" : "0" );
                                //     s.append(" ");
                                // }
                                // System.out.println(s);
                                // StringBuilder ss = new StringBuilder();
                                // for( int i = 0; i < Common.Piece;  i++ )
                                // {
                                //     ss.append( PeerInfo.MyFileBits.get(i) == true ? "1" : "0" );
                                //     ss.append(" ");
                                // }
                                // System.out.println("MyFileBits " + ss);
                                // //Then we send a bitfield back to the other peer
                                // sendMessage(PeerInfo.MyFileBits.toByteArray());
                                
                            }                            
                        }
                        else
                        {
                            System.out.println("Message of length 0 received");
                        }   
                    }
                }
                catch (IOException e) 
                {
                    System.err.println("Connection closed with: " + connectedPeer.HostName);
                    PeerInfo.SetHandshake(connectedPeer.PeerId, false);
                    for (PeerInfo.Pair<Integer, Boolean> b : PeerInfo.hShakeArray) 
                    {
                        if (b.getLeft() != MyPeer.PeerId)
                            System.out.println("PeerId: " + b.getLeft() + " isHandshake: " + b.getRight());
                    }
                }
                catch(Exception e)
                {
                    System.err.println("Error: " + e.getMessage() + "\n");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString(); // stack trace as a string
                    System.out.println(sStackTrace);
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
               // System.out.println("Send message: " + new String(msg, charset));
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        }
    }
}
