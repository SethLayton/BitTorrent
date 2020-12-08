import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.lang.model.util.ElementScanner6;
import javax.sound.midi.SysexMessage;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.util.Random;
import java.nio.file.*;
import java.nio.charset.*;

import java.util.ArrayList;
import java.util.Arrays;

public class peerProcess 
{
    static BlockingQueue<Integer> queue1 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue2 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue3 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue4 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue5 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue6 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue7 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue8 = new LinkedBlockingQueue<Integer>();
    static BlockingQueue<Integer> queue9 = new LinkedBlockingQueue<Integer>();

    static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    static ReadWriteLock haveReadWriteLock = new ReentrantReadWriteLock();
    static ChokingInterval choke;
    static Boolean ending = false;
    
    public static void main(String[] args) throws Exception 
    {
        PeerInfo MyPeer = PeerInfo.getPeerInfo(java.net.InetAddress.getLocalHost().toString().split("/")[0]);

        choke = new ChokingInterval(Common.NumberOfPreferredNeighbors, Common.Peers.size(), Common.UnchokingInterval, Common.OptimisticUnchokingInterval);

        try 
        {
            if (PeerInfo.MyPeerId != Common.GetSmallestPeerId()) 
            {
                // connect to all the previous peers
                for (PeerInfo p : Common.getPeers()) 
                {
                    if (p.PeerId < PeerInfo.MyPeerId) 
                    {
                        Log.Write(MessageFormat.format("Peer {0} makes a connection to Peer {1}.", PeerInfo.MyPeerId, p.PeerId));
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

        // open connection for future peers to connect if its not the last to start
        if (PeerInfo.MyPeerId != Common.GetLargestPeerId()) {
            ServerSocket listener = new ServerSocket(PeerInfo.MyPortNumber);
            listener.setSoTimeout(7*1000);
            System.out.println("The server is running.");
            // Log.Write("The server is running for: " + PeerInfo.MyHostName);
            try 
            {                
                while (!ending) 
                {
                    new Handler(listener.accept(), MyPeer).start();                
                }
            } 
            catch (Exception e)
            {

            }
            finally 
            {
                Log.Write("All peers have the complete file, closing.");
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
        Boolean has = false;
        Boolean unchoked = false;
        Boolean ib1 = false;
        Boolean ib2 = false;
        Boolean ib3 = false;
        Boolean ib4 = false;
        Boolean ib5 = false;
        Boolean ib6 = false;
        Boolean ib7 = false;
        Boolean ib8 = false;
        Boolean ib9 = false;


        public Handler(Socket connection, PeerInfo p) 
        {
            System.out.println("connected from:");
            this.connection = connection;
            this.MyPeer = p;

        }

        public Handler(PeerInfo p, PeerInfo myp) 
        {
            System.out.println("connected to:");
            try 
            {
                this.MyPeer = myp;
                this.connectedPeer = p;
                connection = new Socket(connectedPeer.HostName, connectedPeer.PortNumber);
                client = true;
            } 
            catch (IOException e) 
            {
                //System.err.println("Connection closed with: " + connectedPeer.HostName);
                PeerInfo.SetHandshake(connectedPeer.PeerId, false);
                choke.stopInterval();
                System.exit(0);
            } 
            catch (Exception e) 
            {
                choke.stopInterval();
                System.exit(0);
            }
        }

       
        public void run() 
        {
            try
            {
                //initialize Input and Output streams
                Boolean threadEnd = false;
                has = PeerInfo.MyHasFile;
                //out.flush();
                in = new DataInputStream(connection.getInputStream());      
                out = new DataOutputStream(connection.getOutputStream());  
                try
                {
                    while(true)
                    {
                        if (connectedPeer != null)
                        {
                            for (PeerInfo.Pair<Integer,Boolean> p : PeerInfo.UnchokedNeighbors.unchokedNeighbors) 
                            {
                                
                                if (p.getLeft().intValue() == connectedPeer.PeerId && !connectedPeer.HasFile)
                                {
                                    if (connectedPeer.peerUnChoked ^ p.getRight())
                                    {
                                        if (p.getRight())
                                        {
                                            //send unchoked
                                            System.out.println("Sending unchoke to " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} sends 'unchoke' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createUnchoke());
                                            connectedPeer.peerUnChoked = !connectedPeer.peerUnChoked;
                                        }
                                        else
                                        {
                                            //send choked
                                            System.out.println("Sending  choke to " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} sends 'choke' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createChoke());
                                            connectedPeer.peerUnChoked = !connectedPeer.peerUnChoked;
                                        }
                                    }                                    
                                }
                            }
                        }
                            switch (connectedPeer != null ? connectedPeer.PeerId % 10 : -1) 
                            {
                                case 1:
                                    while (!queue1.isEmpty())
                                    {
                                        int sMsg = queue1.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib1)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib1 = true;
                                    }
                                    break;
                                case 2:
                                    while (!queue2.isEmpty())
                                    {
                                        int sMsg = queue2.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib2)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib2 = true;
                                    }
                                    break;
                                case 3:
                                    while (!queue3.isEmpty())
                                    {
                                        int sMsg = queue3.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib3)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib3 = true;
                                    }
                                    break;
                                case 4:
                                    while (!queue4.isEmpty())
                                    {
                                        int sMsg = queue4.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib4)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib4 = true;
                                    }
                                    break;
                                case 5:
                                    while (!queue5.isEmpty())
                                    {
                                        int sMsg = queue5.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib5)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib5 = true;
                                    }
                                    break;
                                case 6:
                                    while (!queue6.isEmpty())
                                    {
                                        int sMsg = queue6.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib6)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib6 = true;
                                    }
                                    break;
                                case 7:
                                    while (!queue7.isEmpty())
                                    {
                                        int sMsg = queue7.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib7)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib7 = true;
                                    }
                                    break;
                                case 8:
                                    while (!queue8.isEmpty())
                                    {
                                        int sMsg = queue8.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib8)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib8 = true;
                                    }
                                    break;
                                case 9:
                                    while (!queue9.isEmpty())
                                    {
                                        int sMsg = queue9.poll().intValue();
                                        System.out.println("Sending have piece " + sMsg + " to peer: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'have' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createHave(ByteBuffer.allocate(4).putInt(sMsg).array()));
                                        
                                    }
                                    if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece && !ib9)
                                    {
                                            Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            sendMessage(Message.createNotInterested());
                                            ib9 = true;
                                    }
                                    break;
                                default:
                                    break;
                            }   

                        has = true;
                        if (connectedPeer != null)
                        {
                            for (PeerInfo.Pair<Integer,Boolean> hfa : PeerInfo.HasFileArray) 
                            {
                                if (!hfa.getRight())
                                {
                                    has = false;
                                } 
                                
                            }
                        }
                        if (has && connectedPeer != null)
                        {
                            if (!threadEnd)
                            {
                                Thread.sleep(500);
                                threadEnd = true;
                            }
                            else
                            {
                                System.out.println("ending");
                                ending = true;
                                in.close();
                                out.close();
                                choke.stopInterval();
                                this.interrupt();                            
                                return;
                            }
                        }
                        
                        if (client && (connectedPeer == null || !PeerInfo.isHandShake(connectedPeer.PeerId)))
                        {
                            handshakepid = Integer.toString(MyPeer.PeerId).getBytes();
                            Log.Write(MessageFormat.format("Peer {0} sends 'handshake' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                            sendMessage(Common.concat(Message.handshakeheader,Message.handshakezbits,handshakepid));
                        }
                        int length = 0;
                        if (in.available() > 0)
                        {
                            length = in.readInt(); 
                            
                        }
                        byte[] data;
                        if(length > 0) 
                        {    
                            data = new byte[length];                                        
                            in.readFully(data, 0, data.length);
                            if (data != null)
                            {
                                ArrayList<byte[]> fields = Message.parseMessage(data);
                                message = new String(data);
                                if(message.contains("P2PFILESHARINGPROJ") && (connectedPeer == null || !PeerInfo.isHandShake(connectedPeer.PeerId)))
                                {                     
                                    Log.Write(MessageFormat.format("Peer {0} receives handshake from Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));               
                                    //Potential need to change here as splitting could give null exception
                                    connectedPeer = PeerInfo.getPeerInfo(Integer.parseInt(Common.removeBadFormat(message.split("0000000000")[1])));
                                    Log.Write(MessageFormat.format("Peer {0} is connected from Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                    handshakepid = Integer.toString(MyPeer.PeerId).getBytes();
                                    Log.Write(MessageFormat.format("Peer {0} sends 'handshake' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                    sendMessage(Common.concat(Message.handshakeheader,Message.handshakezbits,handshakepid));
                                    PeerInfo.SetHandshake(connectedPeer.PeerId, true);                                
                                                                    
                                }
                                else
                                {  
                                    if (!bitSent && !client)
                                    {
                                        //System.out.println("locking read 3 " + connectedPeer.PeerId);
                                        readWriteLock.readLock().lock();
                                        //System.out.println("locked read 3 " + connectedPeer.PeerId);
                                        BitSet MyFileBitsClone = (BitSet)PeerInfo.MyFileBits.clone();
                                        //System.out.println("unlocking 1 " + connectedPeer.PeerId);
                                        readWriteLock.readLock().unlock();
                                        //System.out.println("unlocked 1 " + connectedPeer.PeerId);

                                        //System.out.println("sending bitfield to: " + connectedPeer.PeerId);
                                        Log.Write(MessageFormat.format("Peer {0} sends 'bitfield' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                        sendMessage(Message.createBitfield(MyFileBitsClone));
                                        bitSent = true;
                                    }
                                    //System.out.println("switch: " + fields.get(1)[0] + " " + connectedPeer.PeerId);                  
                                    switch (fields.get(1)[0])
                                    {
                                        case 0: //choke
                                            //What do here? anything? update some running list?
                                            System.out.println("received choke from: " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} is choked by Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            PeerInfo.myUnChoked = false;
                                            unchoked = false;
                                            break;

                                        case 1: //unchoke
                                            //randomly select a piece that the unchoking person has that we do not have and request that
                                            //PeerInfo.myUnChoked = true;
                                            unchoked = true;
                                            System.out.println("received unchoke from: " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} is unchoked by Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            //System.out.println("locking write 1 " + connectedPeer.PeerId);
                                            readWriteLock.writeLock().lock();
                                            //System.out.println("locked write 1 " + connectedPeer.PeerId);
                                            int index = 0;
                                            ArrayList<Integer> requestList = new ArrayList<>();
                                            BitSet myTemp = (BitSet)PeerInfo.MyFileBits.clone();
                                            BitSet incTemp = (BitSet)connectedPeer.FileBits.clone();
                                            // StringBuilder ssss = new StringBuilder();
                                            // for( int i = 0; i < Common.Piece;  i++ )
                                            // {
                                            //     ssss.append( myTemp.get(i) == true ? "1" : "0" );
                                            //     ssss.append(" ");
                                            // }
                                            // System.out.println("myTemp       " + ssss);

                                            // StringBuilder sss = new StringBuilder();
                                            // for( int i = 0; i < Common.Piece;  i++ )
                                            // {
                                            //     sss.append( incTemp.get(i) == true ? "1" : "0" );
                                            //     sss.append(" ");
                                            // }
                                            // System.out.println("incTemp      " + sss);
                                            myTemp.xor(incTemp);
                                            myTemp.and(incTemp);

                                            // StringBuilder sssssss = new StringBuilder();
                                            // for( int i = 0; i < Common.Piece;  i++ )
                                            // {
                                            //     sssssss.append( myTemp.get(i) == true ? "1" : "0" );
                                            //     sssssss.append(" ");
                                            // }
                                            // System.out.println("myTempfinish " + sssssss);



                                            for (int i = 0; i < myTemp.length(); i++) 
                                            {
                                                if (myTemp.get(i))
                                                    requestList.add(i);
                                            }
                                            // System.out.println(requestList.size());
                                            if (requestList.size() > 0)
                                            {
                                                index = requestList.get(new Random().nextInt(requestList.size())).intValue();                                                
                                                PeerInfo.pReadWriteLock.writeLock().lock();
                                                int[] rinp =  PeerInfo.requestedArray.get(index);
                                                Boolean complete = false;
                                                Boolean allRequested = false;
                                                if (rinp[0] != 1)
                                                {
                                                        rinp[0] = 1;
                                                        rinp[1] = 0;
                                                        PeerInfo.requestedArray.set(index, rinp);
                                                        System.out.println("sending request piece (" + index + ") to: " + connectedPeer.PeerId);
                                                        Log.Write(MessageFormat.format("Peer {0} sends 'request' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                        sendMessage(Message.createRequest(index));
                                                }
                                                else
                                                {                                            
                                                    while (rinp[0] == 1 && !complete && !allRequested)
                                                    {
                                                        //System.out.println("y " + connectedPeer.PeerId);
                                                        index = requestList.get(new Random().nextInt(requestList.size())).intValue();
                                                        rinp =  PeerInfo.requestedArray.get(index);
                                                        if (rinp[0] != 1)
                                                        {
                                                            rinp[0] = 1;
                                                            rinp[1] = 0;
                                                            PeerInfo.requestedArray.set(index, rinp);
                                                            System.out.println("sending request piece (" + index + ") to: " + connectedPeer.PeerId);
                                                            Log.Write(MessageFormat.format("Peer {0} sends 'request' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                            sendMessage(Message.createRequest(index));
                                                            complete = true;
                                                        }

                                                        for (Integer r : requestList)
                                                        {
                                                            if (PeerInfo.requestedArray.get(r.intValue())[0] != 1)
                                                            {
                                                                allRequested = false;
                                                                break;
                                                            }
                                                            allRequested = true;
                                                        }
                                                        if(allRequested)
                                                        {
                                                            rinp[0] = 1;
                                                            rinp[1] = 0;
                                                            index = requestList.get(new Random().nextInt(requestList.size())).intValue();
                                                            PeerInfo.requestedArray.set(index, rinp);
                                                            System.out.println("sending request piece (" + index + ") to: " + connectedPeer.PeerId);
                                                            Log.Write(MessageFormat.format("Peer {0} sends 'request' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                            sendMessage(Message.createRequest(index)); 
                                                        }
                                                    }
                                                }
                                                PeerInfo.pReadWriteLock.writeLock().unlock();
                                            }
                                            //System.out.println("unlocking write 1 " + connectedPeer.PeerId);
                                            readWriteLock.writeLock().unlock();
                                            //System.out.println("unlocked write 1 " + connectedPeer.PeerId);
                                            break;

                                        case 2: //interested
                                            System.out.println("received interested from: " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} received the 'interested' message from Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            PeerInfo.SetInterested(connectedPeer.PeerId, "Interested");
                                            connectedPeer.interested = true;
                                            break;

                                        case 3: //not interested
                                            System.out.println("received not interested from: " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} received the 'not interested' message from Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            PeerInfo.SetInterested(connectedPeer.PeerId, "NotInterested");
                                            connectedPeer.interested = false;
                                            break;

                                        case 4: //have
                                            ByteBuffer bb = ByteBuffer.wrap(fields.get(2));
                                            int temp1 = bb.getInt();
                                            System.out.println("received have piece " + temp1 +" from: " + connectedPeer.PeerId);
                                            Log.Write(MessageFormat.format("Peer {0} received the 'have' message from Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            //System.out.println("locking write 2 " + connectedPeer.PeerId);
                                            readWriteLock.writeLock().lock();
                                            //System.out.println("locked write 2 " + connectedPeer.PeerId);
                                            ArrayList<Integer> haveRequestList = new ArrayList<>();
                                            BitSet haveMyTemp = (BitSet)PeerInfo.MyFileBits.clone();
                                            connectedPeer.FileBits.set(temp1);
                                            BitSet haveIncTemp = (BitSet)connectedPeer.FileBits.clone();
                                            haveMyTemp.xor(haveIncTemp);
                                            haveMyTemp.and(haveIncTemp);
                                            for (int i = 0; i < haveMyTemp.length(); i++) 
                                            {
                                                if (haveMyTemp.get(i))
                                                    haveRequestList.add(i);
                                            }                                            
                                                                                       
                                            readWriteLock.writeLock().unlock();
                                            if (connectedPeer.FileBits.nextClearBit(0) >= Common.Piece)
                                            {
                                                System.out.println("*connectedpeer: " + connectedPeer.PeerId + "has file");
                                                connectedPeer.HasFile = true;
                                                haveReadWriteLock.writeLock().lock();
                                                PeerInfo.SetHaveFileArray(connectedPeer.PeerId, connectedPeer.HasFile);
                                                haveReadWriteLock.writeLock().unlock();
                                            }
                                            if (connectedPeer.HasFile && PeerInfo.MyHasFile)
                                                break;
                                            if (haveRequestList.size() > 0)
                                            {
                                                System.out.println("sending interested to: " + connectedPeer.PeerId);
                                                Log.Write(MessageFormat.format("Peer {0} sends 'interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                sendMessage(Message.createInterested());
                                            }
                                            else 
                                            {
                                                System.out.println("sending NotInterested to: " + connectedPeer.PeerId);
                                                Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                sendMessage(Message.createNotInterested());
                                            } 
                                            break;

                                        case 5: //bitfield
                                            Log.Write(MessageFormat.format("Peer {0} receives bitfield from Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                            //System.out.println("received bitfield from: " + connectedPeer.PeerId);
                                            connectedPeer.FileBits = BitSet.valueOf(fields.get(2));
                                            if (connectedPeer.FileBits.nextClearBit(0) >= Common.Piece)
                                            {
                                                System.out.println("*connectedpeer: " + connectedPeer.PeerId + "has file");
                                                connectedPeer.HasFile = true;
                                                haveReadWriteLock.writeLock().lock();
                                                PeerInfo.SetHaveFileArray(connectedPeer.PeerId, connectedPeer.HasFile);
                                                haveReadWriteLock.writeLock().unlock();
                                            }
                                            //PeerInfo.SetBitField(connectedPeer.PeerId, connectedPeer.FileBits);
                                            if (client && !bitSent)
                                            {
                                                //System.out.println("locking read 1 " + connectedPeer.PeerId);
                                                readWriteLock.readLock().lock();
                                                //System.out.println("locked read 1 " + connectedPeer.PeerId);
                                                BitSet BitFieldMyFileBitsClone = (BitSet)PeerInfo.MyFileBits.clone();
                                                BitSet temp = (BitSet)PeerInfo.MyFileBits.clone();
                                                //System.out.println("unlocking read 2 " + connectedPeer.PeerId);
                                                readWriteLock.readLock().unlock();
                                                //System.out.println("unlocked read 2 " + connectedPeer.PeerId);

                                                //System.out.println("sending bitfield to: " + connectedPeer.PeerId);
                                                Log.Write(MessageFormat.format("Peer {0} sends 'bitfield' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                sendMessage(Message.createBitfield(BitFieldMyFileBitsClone));
                                                BitSet temp2 = (BitSet)connectedPeer.FileBits.clone();
                                                temp.xor(temp2);                                            
                                                temp2.and(temp);
                                                if (!temp2.isEmpty())
                                                {
                                                    System.out.println("sending interested to: " + connectedPeer.PeerId);
                                                    Log.Write(MessageFormat.format("Peer {0} sends 'interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                    sendMessage(Message.createInterested());
                                                }
                                                else
                                                {
                                                    System.out.println("sending not interested to: " + connectedPeer.PeerId);
                                                    Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                    sendMessage(Message.createNotInterested());
                                                }
                                                bitSent = true;
                                            }
                                            else
                                            {
                                                //System.out.println("locking read 2 " + connectedPeer.PeerId);
                                                readWriteLock.readLock().lock();
                                                //System.out.println("locked read 2 " + connectedPeer.PeerId);
                                                BitSet temp = (BitSet)PeerInfo.MyFileBits.clone();
                                                //System.out.println("unlocking read 3 " + connectedPeer.PeerId);
                                                readWriteLock.readLock().unlock();
                                                //System.out.println("unlocked read 3 " + connectedPeer.PeerId);
                                                BitSet temp2 = (BitSet)connectedPeer.FileBits.clone();
                                                temp.xor(temp2);                                            
                                                temp2.and(temp);
                                                if (!temp2.isEmpty())
                                                {
                                                    System.out.println("sending interested to: " + connectedPeer.PeerId);
                                                    Log.Write(MessageFormat.format("Peer {0} sends 'interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                    sendMessage(Message.createInterested());
                                                }
                                                else
                                                {
                                                    System.out.println("sending not interested to: " + connectedPeer.PeerId);
                                                    Log.Write(MessageFormat.format("Peer {0} sends 'not interested' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                    sendMessage(Message.createNotInterested());
                                                }

                                            }
                                            break;

                                        case 6: //request
                                            ByteBuffer rbb = ByteBuffer.wrap(fields.get(2));
                                            int rindex = rbb.getInt();
                                            if (connectedPeer.peerUnChoked)
                                            {  
                                                System.out.println("received request for (" + rindex + ") from: " + connectedPeer.PeerId);
                                                int rstart = rindex * Common.PieceSize;
                                                byte[] filechunk = Arrays.copyOfRange(PeerInfo.MyFile, rstart, (rstart + Common.PieceSize > Common.FileSize ? Common.FileSize : rstart + Common.PieceSize));
                                                System.out.println("sending piece (" + rindex + ") to: " + connectedPeer.PeerId);
                                                Log.Write(MessageFormat.format("Peer {0} sends 'piece' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                sendMessage(Message.createPiece(Common.concat(fields.get(2), filechunk), filechunk.length), connectedPeer.PeerId);
                                                PeerInfo.DownloadRate.setDownloadRate(connectedPeer.PeerId, filechunk.length);
                                                //System.out.println("Completed setting download for: " + connectedPeer.PeerId);
                                            }
                                            else
                                            {
                                                System.out.println("received request for (" + rindex + ") from: " + connectedPeer.PeerId + " but peer is choked");
                                            }
                                            break;

                                        case 7: //piece
                                            ByteBuffer pbb = ByteBuffer.wrap(Arrays.copyOfRange(fields.get(2),0,4));
                                            int pindex = pbb.getInt();
                                            System.out.println("received piece (" + pindex + ") from: " + connectedPeer.PeerId);
                                            ByteBuffer pbb1 = ByteBuffer.wrap(Arrays.copyOfRange(fields.get(0),0,4));
                                            int plength = pbb1.getInt();
                                            //System.out.println("locking write 3 " + connectedPeer.PeerId);
                                            readWriteLock.writeLock().lock();
                                            //System.out.println("locked write 3 " + connectedPeer.PeerId);
                                            PeerInfo.MyFileBits.set(pindex);
                                            //BitSet tTemp = (BitSet)PeerInfo.MyFileBits.clone();
                                            // StringBuilder sssssss11 = new StringBuilder();
                                            // for( int i = 0; i < Common.Piece;  i++ )
                                            // {
                                            //     sssssss11.append( tTemp.get(i) == true ? "1" : "0" );
                                            //     sssssss11.append(" ");
                                            // }
                                            // System.out.println("My Filebits: "+ sssssss11);
                                                  
                                            System.arraycopy(fields.get(2), 4, PeerInfo.MyFile, pindex * Common.PieceSize , Common.PieceSize <= plength ? Common.PieceSize : plength);
                                            if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece)
                                            {
                                                PeerInfo.MyHasFile = true;
                                                Log.Write("Peer " + PeerInfo.MyPeerId + " has downloaded the complete file.");
                                                haveReadWriteLock.writeLock().lock();
                                                PeerInfo.SetHaveFileArray(PeerInfo.MyPeerId, PeerInfo.MyHasFile);
                                                haveReadWriteLock.writeLock().unlock();
                                                File output = new File("peer_" + PeerInfo.MyPeerId + "/" + Common.FileName);
                                                OutputStream ops = new FileOutputStream(output);
                                                ops.write(PeerInfo.MyFile);
                                                ops.close();                                               
                                            }

                                            //for (PeerInfo pi : Common.Peers) 
                                            //{
                                                //if (pi.PeerId != PeerInfo.MyPeerId)
                                                //{
                                            for (PeerInfo.Pair<Integer,Boolean> hs : PeerInfo.hShakeArray) 
                                            {
                                                if (hs.getLeft().intValue() != PeerInfo.MyPeerId && hs.getRight())
                                                {                                                            
                                                    switch (hs.getLeft().intValue() % 10) 
                                                    {
                                                        case 1:
                                                            if (!queue1.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue1.put(pindex);
                                                            }
                                                            break;
                                                        case 2:
                                                            if (!queue2.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue2.put(pindex);
                                                            }
                                                            break;
                                                        case 3:
                                                            if (!queue3.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue3.put(pindex);
                                                            }
                                                            break;
                                                        case 4:
                                                            if (!queue4.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue4.put(pindex);
                                                            }
                                                            break;
                                                        case 5:
                                                            if (!queue5.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue5.put(pindex);
                                                            }
                                                            break;
                                                        case 6:
                                                            if (!queue6.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue6.put(pindex);
                                                            }
                                                            break;
                                                        case 7:
                                                            if (!queue7.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue7.put(pindex);
                                                            }
                                                            break;
                                                        case 8:
                                                            if (!queue8.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue8.put(pindex);
                                                            }
                                                            break;
                                                        case 9:
                                                            if (!queue9.contains(pindex))
                                                            {
                                                                //System.out.println("Putting " + pindex + " on stack for peer: " + hs.getLeft());
                                                                queue9.put(pindex);
                                                            }
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                }
                                            }
                                                    
                                                //}
                                            //}

                                            if (PeerInfo.MyFileBits.nextClearBit(0) >= Common.Piece)
                                            {
                                                //System.out.println("unlocking write 3.5 " + connectedPeer.PeerId);
                                                readWriteLock.writeLock().unlock();
                                                //System.out.println("unlocked write 3.5 " + connectedPeer.PeerId);
                                                break;
                                            }

                                            int pieceRequestIndex = 0;
                                            ArrayList<Integer> piecerequestList = new ArrayList<>();
                                            BitSet pieceMyTemp = (BitSet)PeerInfo.MyFileBits.clone();
                                            
                                            // Calculate the amount of pieces we have for log
                                            int current_total = 0;
                                            for (int i = 0; i < pieceMyTemp.length(); i++) 
                                            {
                                                if (pieceMyTemp.get(i)) {
                                                    current_total++;
                                                }
                                            }
                                            Log.Write(MessageFormat.format("Peer {0} has downloaded the piece {1} from Peer {2}. Now the number of pieces it has is {4}.", PeerInfo.MyPeerId, pindex, connectedPeer.PeerId, current_total));
                                            
                                            BitSet pieceIncTemp = (BitSet)connectedPeer.FileBits.clone();
                                            pieceMyTemp.xor(pieceIncTemp);
                                            pieceMyTemp.and(pieceIncTemp);


                                            for (int i = 0; i < pieceMyTemp.length(); i++) 
                                            {
                                                if (pieceMyTemp.get(i))
                                                    piecerequestList.add(i);
                                            }

                                            //if (unchoked)
                                            //{
                                            if (piecerequestList.size() > 0)
                                            {
                                                pieceRequestIndex = piecerequestList.get(new Random().nextInt(piecerequestList.size())).intValue();
                                                PeerInfo.pReadWriteLock.writeLock().lock();
                                                int[] rinp =  PeerInfo.requestedArray.get(pieceRequestIndex);
                                                Boolean complete = false;
                                                Boolean allRequested = false;
                                                if (rinp[0] != 1)
                                                {
                                                    rinp[0] = 1;
                                                    rinp[1] = 0;
                                                    PeerInfo.requestedArray.set(pieceRequestIndex, rinp);
                                                    System.out.println("sending request piece (" + pieceRequestIndex + ") to: " + connectedPeer.PeerId);
                                                    Log.Write(MessageFormat.format("Peer {0} sends 'request' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                    sendMessage(Message.createRequest(pieceRequestIndex));
                                                }
                                                else
                                                {
                                                    while (rinp[0] == 1 && !complete && !allRequested)
                                                    {
                                                        //System.out.println("x " + connectedPeer.PeerId);
                                                        pieceRequestIndex = piecerequestList.get(new Random().nextInt(piecerequestList.size())).intValue();
                                                        rinp =  PeerInfo.requestedArray.get(pieceRequestIndex);
                                                        if (rinp[0] != 1)
                                                        {
                                                            rinp[0] = 1;
                                                            rinp[1] = 0;
                                                            PeerInfo.requestedArray.set(pieceRequestIndex, rinp);
                                                            System.out.println("sending request piece (" + pieceRequestIndex + ") to: " + connectedPeer.PeerId);
                                                            Log.Write(MessageFormat.format("Peer {0} sends 'request' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                            sendMessage(Message.createRequest(pieceRequestIndex));
                                                            complete = true;
                                                        }

                                                        for (Integer r : piecerequestList)
                                                        {
                                                            if (PeerInfo.requestedArray.get(r.intValue())[0] != 1)
                                                            {
                                                                allRequested = false;
                                                                break;
                                                            }
                                                            allRequested = true;
                                                        }
                                                        if(allRequested)
                                                        {
                                                            rinp[0] = 1;
                                                            rinp[1] = 0;
                                                            pieceRequestIndex = piecerequestList.get(new Random().nextInt(piecerequestList.size())).intValue();
                                                            PeerInfo.requestedArray.set(pieceRequestIndex, rinp);
                                                            System.out.println("sending request piece (" + pieceRequestIndex + ") to: " + connectedPeer.PeerId);
                                                            Log.Write(MessageFormat.format("Peer {0} sends 'request' to Peer {1}.", PeerInfo.MyPeerId, connectedPeer.PeerId));
                                                            sendMessage(Message.createRequest(pieceRequestIndex)); 
                                                        }
                                                    }
                                                }                                                
                                                PeerInfo.pReadWriteLock.writeLock().unlock();  
                                            }
                                            //System.out.println("unlocking write 3 " + connectedPeer.PeerId);
                                            readWriteLock.writeLock().unlock();
                                            //System.out.println("unlocked write 3 " + connectedPeer.PeerId);
                                            break;
                                        default:
                                            break;
                                    }                                
                                }  
                            }                          
                        } 
                    }
                }
                catch (IOException e) 
                {
                    System.err.println("Connection closed with: " + connectedPeer.HostName);
                    System.err.println(e.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString(); // stack trace as a string
                    System.out.println(sStackTrace);
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
                System.err.println("Error: " + ioException.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ioException.printStackTrace(pw);
                String sStackTrace = sw.toString();
                System.out.println(sStackTrace);
            }
            catch(Exception e)
            {
                System.out.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
                System.out.println();
            }
            finally
            {
                //Close connections
                //System.out.println("Disconnect with Client ");
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
        public void sendMessage(byte[] msg) throws IOException
        {
            //out = new DataOutputStream(connection.getOutputStream());
            try
            {
                out.writeInt(msg.length);
                out.write(msg);
                out.flush();
            }
            catch(IOException ioException)
            {
                //ioException.printStackTrace();
            }
            // finally
            // {
            //     out.close();                
            // }
        }  
        public void sendMessage(byte[] msg, int in) throws IOException
        {
            try
            {
                //out = new DataOutputStream(connection.getOutputStream());
                //System.out.println(System.currentTimeMillis() + " starting length message: " + msg.length +" to: " + in);
                out.writeInt(msg.length);
                ArrayList<byte[]> a = Message.parseMessage(msg);
                ByteBuffer pbb = ByteBuffer.wrap(Arrays.copyOfRange(a.get(2),0,4));
                //int pindex = pbb.getInt();
                //System.out.println(System.currentTimeMillis() + "starting message: " + a.get(0) + " " + a.get(1)[0] + " " + pindex + a.get(2) + " to: " + in);
                out.write(msg);
                //System.out.println(System.currentTimeMillis() + "completed message to: " + in);
                out.flush();
            }
            catch(Exception e)
            {
                System.out.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
                System.out.println();
            }
            finally
            {
                //out.close();                
            }
        }  
    }
}
