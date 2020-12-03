import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.Random;
import java.nio.file.*;
import java.nio.charset.*;

import java.util.ArrayList;
import java.util.Arrays;

public class peerProcess 
{
    static BlockingQueue<int[]> queue = new LinkedBlockingQueue<int[]>();
    static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public static void main(String[] args) throws Exception 
    {
        PeerInfo MyPeer = PeerInfo.getPeerInfo(java.net.InetAddress.getLocalHost().toString().split("/")[0]);

        System.out.println("starting choking  ");
        ChokingInterval choke = new ChokingInterval(Common.NumberOfPreferredNeighbors, Common.Peers.size(), Common.UnchokingInterval, Common.OptimisticUnchokingInterval);

        try 
        {
            if (PeerInfo.MyPeerId != Common.GetSmallestPeerId()) 
            {
                // connect to all the previous peers
                for (PeerInfo p : Common.getPeers()) 
                {
                    if (p.PeerId < PeerInfo.MyPeerId) 
                    {
                        //System.out.println("Attempting to connect to client: " + p.PeerId + " " + p.HostName);
                        new Handler(p, MyPeer).start();
                        //new Handler(p, MyPeer, true).start();
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

        // open connection for future peers to connect if its not the last to start
        if (PeerInfo.MyPeerId != Common.GetLargestPeerId()) {
            ServerSocket listener = new ServerSocket(PeerInfo.MyPortNumber);
            System.out.println("The server is running.");
            Log.Write("The server is running for: " + PeerInfo.MyHostName);
            try 
            {
                while (true) 
                {
                    new Handler(listener.accept(), MyPeer).start();
                }
            } 
            finally 
            {
                listener.close();
            }
        }
        

    }

    /**
     * A handler thread class. Handlers are spawned from the listening loop and are
     * responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread 
    {

        private String message;
        private Socket connection;
        private DataInputStream in;
        private DataOutputStream out;
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

        public Handler(PeerInfo p, PeerInfo myp) {
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
            catch (Exception e) 
            {
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
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
                        if (connectedPeer != null)
                        {
                            for (PeerInfo.Pair<Integer,Boolean> p : PeerInfo.UnchokedNeighbors.unchokedNeighbors) 
                            {
                                if (p.getLeft() == PeerInfo.MyPeerId)
                                {
                                    if (connectedPeer.peerUnChoked ^ p.getRight())
                                    {
                                        if (p.getRight())
                                        {
                                            //send unchoked
                                            System.out.println("Sending unchoke to " + connectedPeer.PeerId);
                                            sendMessage(Message.createUnchoke());
                                            connectedPeer.peerUnChoked = !connectedPeer.peerUnChoked;
                                        }
                                        else
                                        {
                                            //send choked
                                            System.out.println("Sending choke to " + connectedPeer.PeerId);
                                            sendMessage(Message.createChoke());
                                            connectedPeer.peerUnChoked = !connectedPeer.peerUnChoked;
                                        }
                                    }
                                    
                                }
                            }
                        }
                        int msg[] = queue.peek();
                        if (msg != null  && connectedPeer.PeerId == msg[0])
                        {
                            int sMsg[] = queue.poll();
                            int index = sMsg[1];
                            System.out.println("Sending have piece " + index + " to peer: " + connectedPeer.PeerId);
                            sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(index).array()));
                        }
                        if (client && (connectedPeer == null || !PeerInfo.isHandShake(connectedPeer.PeerId)))
                        {
                            handshakepid = Integer.toString(MyPeer.PeerId).getBytes();
                            sendMessage(Common.concat(Message.handshakeheader,Message.handshakezbits,handshakepid));
                        }
                        int length = 0;
                        if (in.available() > 0)
                        {
                            length = in.readInt();
                        }
                        byte[] data;
                        if(length>0) 
                        {                        
                            data = new byte[length];
                            in.readFully(data, 0, data.length);
                            if (data != null)
                            {
                                ArrayList<byte[]> fields = Message.parseMessage(data);
                                message = new String(data);
                                if(message.contains("P2PFILESHARINGPROJ") && (connectedPeer == null || !PeerInfo.isHandShake(connectedPeer.PeerId)))
                                {
                                    
                                    //Potential need to change here as splitting could give null exception
                                    connectedPeer = PeerInfo.getPeerInfo(Integer.parseInt(Common.removeBadFormat(message.split("0000000000")[1])));
                                    if (!client && connectedPeer != null)
                                    {
                                        //new Handler(connectedPeer,MyPeer, true).start();
                                    }
                                    Log.Write(MessageFormat.format("Peer {0} is connected from Peer {1}", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                    handshakepid = Integer.toString(MyPeer.PeerId).getBytes();

                                    sendMessage(Common.concat(Message.handshakeheader,Message.handshakezbits,handshakepid));
                                    PeerInfo.SetHandshake(connectedPeer.PeerId, true);                                
                                                                    
                                }
                                else
                                {  
                                    if (!bitSent && !client)
                                    {
                                        readWriteLock.readLock().lock();
                                        BitSet MyFileBitsClone = (BitSet)PeerInfo.MyFileBits.clone();
                                        readWriteLock.readLock().unlock();

                                        System.out.println("sending bitfield to: " + connectedPeer.PeerId);
                                        sendMessage(Message.createBitfield(MyFileBitsClone));
                                        bitSent = true;
                                    }
                                                                      

                                    switch (fields.get(1)[0])
                                    {
                                        case 0: //choke
                                            //What do here? anything? update some running list?
                                            PeerInfo.myUnChoked = false;
                                            break;

                                        case 1: //unchoke
                                            //randomly select a piece that the unchoking person has that we do not have and request that
                                            //TODO: I think we need to set the piece that we are requesting as "IHave" here in the BitSet vs
                                            //Doing it when we actually get the piece in. So we can avoid requesting the same piece twice easier
                                            //There could still be the issue where we randomly request the same piece here if two threads are at this
                                            //unchoke requesting phase at the same time, but it's much smaller
                                            PeerInfo.myUnChoked = true;
                                            System.out.println("received unchoke from: " + connectedPeer.PeerId);
                                            readWriteLock.writeLock().lock();
                                            int index = 0;
                                            ArrayList<Integer> requestList = new ArrayList<>();
                                            BitSet myTemp = (BitSet)PeerInfo.MyFileBits.clone();
                                            BitSet incTemp = (BitSet)connectedPeer.FileBits.clone();
                                            myTemp.xor(incTemp);
                                            BitSet requestTemp = (BitSet)myTemp.clone(); 
                                            requestTemp.andNot(incTemp);
                                            
                                            while (index != -1)
                                            {
                                                index = requestTemp.nextSetBit(index);
                                                if (index != -1)
                                                {
                                                    requestList.add(index);
                                                }
                                            }
                                            //DO the bitset Setting here
                                            index = requestList.get(new Random().nextInt(requestList.size())).intValue();
                                            System.out.println("sending request piece (" + index + ") to: " + connectedPeer.PeerId);
                                            sendMessage(Message.createRequest(index));
                                            PeerInfo.MyFileBits.set(index);
                                            readWriteLock.writeLock().unlock();
                                            break;

                                        case 2: //interested
                                            System.out.println("received interested from: " + connectedPeer.PeerId);
                                            PeerInfo.SetInterested(connectedPeer.PeerId, "Interested");
                                            connectedPeer.interested = true;
                                            break;

                                        case 3: //not interested
                                            System.out.println("received not interested from: " + connectedPeer.PeerId);
                                            PeerInfo.SetInterested(connectedPeer.PeerId, "NotInterested");
                                            connectedPeer.interested = false;
                                            // if (PeerInfo.MyPeerId == 1004 && connectedPeer.PeerId == 1001)
                                            // {
                                            //     System.out.println("sending request for piece 3 to : " + connectedPeer.PeerId);
                                            //     sendMessage(Message.createRequest(3));
                                            // }
                                            break;

                                        case 4: //have
                                            System.out.println("received have from: " + connectedPeer.PeerId);
                                            ByteBuffer bb = ByteBuffer.wrap(fields.get(2));
                                            int temp1 = bb.getInt();
                                            readWriteLock.readLock().lock();
                                            BitSet HaveMyFileBitsClone = (BitSet)PeerInfo.MyFileBits.clone();
                                            readWriteLock.readLock().unlock();
                                            if (!HaveMyFileBitsClone.get(temp1))
                                            {
                                                System.out.println("sending interested to: " + connectedPeer.PeerId);
                                                sendMessage(Message.createInterested());
                                            }
                                            //No need to thread safe access as this FileBits is only for this thread
                                            connectedPeer.FileBits.set(temp1);
                                            break;

                                        case 5: //bitfield
                                            System.out.println("received bitfield from: " + connectedPeer.PeerId);
                                            connectedPeer.FileBits = BitSet.valueOf(fields.get(2));
                                            PeerInfo.SetBitField(connectedPeer.PeerId, connectedPeer.FileBits);
                                            if (client && !bitSent)
                                            {
                                                readWriteLock.readLock().lock();
                                                BitSet BitFieldMyFileBitsClone = (BitSet)PeerInfo.MyFileBits.clone();
                                                BitSet temp = (BitSet)PeerInfo.MyFileBits.clone();
                                                readWriteLock.readLock().unlock();
                                                System.out.println("sending bitfield to: " + connectedPeer.PeerId);
                                                sendMessage(Message.createBitfield(BitFieldMyFileBitsClone));
                                                BitSet temp2 = (BitSet)connectedPeer.FileBits.clone();
                                                temp.xor(temp2);                                            
                                                temp2.and(temp);
                                                if (!temp2.isEmpty())
                                                {
                                                    System.out.println("sending interested to: " + connectedPeer.PeerId);
                                                    sendMessage(Message.createInterested());
                                                }
                                                else
                                                {
                                                    System.out.println("sending not interested to: " + connectedPeer.PeerId);
                                                    sendMessage(Message.createNotInterested());
                                                }
                                                bitSent = true;
                                            }
                                            else
                                            {
                                                readWriteLock.readLock().lock();  
                                                BitSet temp = (BitSet)PeerInfo.MyFileBits.clone();
                                                readWriteLock.readLock().unlock();  
                                                BitSet temp2 = (BitSet)connectedPeer.FileBits.clone();
                                                temp.xor(temp2);                                            
                                                temp2.and(temp);
                                                if (!temp2.isEmpty())
                                                {
                                                    System.out.println("sending interested to: " + connectedPeer.PeerId);
                                                    sendMessage(Message.createInterested());
                                                }
                                                else
                                                {
                                                    System.out.println("sending not interested to: " + connectedPeer.PeerId);
                                                    sendMessage(Message.createNotInterested());
                                                }

                                            }
                                            break;

                                        case 6: //request
                                            if (PeerInfo.myUnChoked)
                                            {
                                                System.out.println("received request from: " + connectedPeer.PeerId);
                                                ByteBuffer rbb = ByteBuffer.wrap(fields.get(2));
                                                int rindex = rbb.getInt();
                                                int rstart = rindex * Common.PieceSize;
                                                byte[] filechunk = Arrays.copyOfRange(PeerInfo.MyFile, rstart, rstart + Common.PieceSize);
                                                System.out.println("sending piece (" + rindex + ") to: " + connectedPeer.PeerId);
                                                sendMessage(Message.createPiece(Common.concat(fields.get(2), filechunk)));
                                                connectedPeer.downloadRate += filechunk.length;
                                                PeerInfo.DownloadRate.setDownloadRate(connectedPeer.PeerId, connectedPeer.downloadRate);

                                            }
                                            break;

                                        case 7: //piece
                                            System.out.println("received piece from: " + connectedPeer.PeerId);
                                            ByteBuffer pbb = ByteBuffer.wrap(Arrays.copyOfRange(fields.get(2),0,4));
                                            int pindex = pbb.getInt();

                                            //Might need this here (unless the unchoke requesting logic works see above unchoke section)
                                            readWriteLock.writeLock().lock();
                                            PeerInfo.MyFileBits.set(pindex);
                                                                                
                                            
                                            StringBuilder ss = new StringBuilder();
                                            for( int i = 0; i < Common.Piece;  i++ )
                                            {
                                                ss.append( PeerInfo.MyFileBits.get(i) == true ? "1" : "0" );
                                                ss.append(" ");
                                            }
                                            System.out.println("MyFileBits " + ss);
                                            readWriteLock.writeLock().unlock(); 
                                            System.arraycopy(fields.get(2), 4, PeerInfo.MyFile, pindex, Common.PieceSize);

                                            //TODO: Handle sending the 'have' message to all other peers.
                                            for (PeerInfo pi : Common.Peers) 
                                            {
                                                if (pi.PeerId != PeerInfo.MyPeerId && pi.PeerId != connectedPeer.PeerId)
                                                {
                                                    System.out.println("putting {" + pi.PeerId + "," + pindex + "} on the stack");
                                                    queue.put(new int[] {pi.PeerId, pindex});
                                                }
                                            }
                                            break;
                                    }                                
                                }  
                            }  
                            // for (int i = 0; i < PeerInfo.hShakeArray.size(); i++) 
                            // {
                            //     if (PeerInfo.hShakeArray.get(i).getLeft() != MyPeer.PeerId)
                            //     {
                            //         StringBuilder ss = new StringBuilder();
                            //         for( int j = 0; j < Common.Piece;  j++ )
                            //         {
                            //             ss.append( PeerInfo.bitFieldArray.get(i).getRight().get(j) == true ? "1" : "0" );
                            //             ss.append(" ");
                            //         }
                            //         System.out.println("PeerId: " + PeerInfo.hShakeArray.get(i).getLeft() 
                            //         + " isHShake: " + PeerInfo.hShakeArray.get(i).getRight()
                            //         + " Interested: " + PeerInfo.interestedArray.get(i).getRight()
                            //         + " bitField: " + ss);
                            //     }
                            // }                        
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
            }
            catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
        }       
    }
}
