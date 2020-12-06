import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Object;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PeerInfo {
    // Information about ALL peers. This is updated dynamically while running
    public int PeerId;
    public String HostName;
    public int PortNumber;
    public boolean HasFile = false;
    public BitSet FileBits;
    public boolean peerUnChoked = false;
    public boolean optUnchoked;
    public Integer downloadRate;
    public boolean interested;
    public static List<Pair<Integer, Boolean>> hShakeArray = new ArrayList<Pair<Integer, Boolean>>();
    public static List<Pair<Integer, String>> interestedArray = new ArrayList<Pair<Integer, String>>();
    public static List<Pair<Integer, Boolean>> HasFileArray = new ArrayList<Pair<Integer, Boolean>>();
    public static List<int[]> requestedArray = new ArrayList<int[]>(Common.Piece);
    // Locally running processes information. This is set from the singleton on
    // startup
    // MyFileBits is set dynamically as the process runs
    public static int MyPeerId;
    public static String MyHostName;
    public static int MyPortNumber;
    public static boolean MyHasFile;
    public static BitSet MyFileBits;
    public static byte[] MyFile;
    public static boolean myUnChoked = false;

    // Singleton variables
    public static int Pieces;
    private static boolean _init = false;
    private static boolean _init_ = false;

    static ReadWriteLock pReadWriteLock = new ReentrantReadWriteLock();

    public static class DownloadRate 
    {
        // This is a 'globally' accessed list. Needs to be thread safe
        public static List<Pair<Integer, Integer>> dRateList = new ArrayList<Pair<Integer, Integer>>();

        public DownloadRate() 
        {
            try 
            {
                List<PeerInfo> pInf = Common.getPeers();
                for (PeerInfo p : pInf) 
                {
                    if (p.PeerId != MyPeerId)
                        dRateList.add(new Pair<Integer, Integer>(p.PeerId, 0));
                }
            } 
            catch (Exception e) 
            {
                Log.Write("Exception in downloadRate default constructor: " + e.getMessage());
                System.out.println("Exception in downloadRate default constructor: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }

        }

        public static Integer getHighestDownload() 
        {
            // default instantiate this return value
            Integer highDownload = dRateList.get(0).getRight();
            try 
            {
                for (Pair<Integer, Integer> p : dRateList) 
                {
                    if (p.getRight() > highDownload)
                        highDownload = p.getRight();
                }
            } 
            catch (Exception e) 
            {
                Log.Write("Exception in getHighestDownload: " + e.getMessage());
                System.out.println("Exception in getHighestDownload: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }

            return highDownload;
        }

        public static Integer[] getHighestDownloadRates() 
        {
            // default instantiate this return value
            List<Integer> newlist = new ArrayList<>();
            try 
            {
                for (Pair<Integer, Integer> p : dRateList) 
                {
                    for(Pair<Integer,Boolean> d : hShakeArray)   
                    {    
                        for(Pair<Integer,String> b : interestedArray)   
                        { 
                            if (d.getLeft().intValue() == p.getLeft().intValue() && b.getLeft().intValue() == p.getLeft().intValue() && d.getRight() && b.getRight() == "Interested")   
                            { 
                                newlist.add(p.getLeft());
                            }
                        }
                    }
                   
                }
            
            } 
            catch (Exception e) 
            {
                Log.Write("Exception in getHighestDownloadRates: " + e.getMessage());
                System.out.println("Exception in getHighestDownloadRates: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }
            if (newlist.isEmpty())
            {
                return null;
            }
            Collections.sort(newlist, Collections.reverseOrder());
            Integer[] highDownloads = new Integer[Common.NumberOfPreferredNeighbors <= newlist.size() ? Common.NumberOfPreferredNeighbors : newlist.size()];
            for(int i = 0; i < highDownloads.length; i++) 
            {
                highDownloads[i] = newlist.get(i);
            }
            
            return highDownloads;
        }

        // reset the rest of the list back to a 0 download rate
        public static void resetDownloadRates() 
        {
            try 
            {
                dRateList.clear();
                List<PeerInfo> pInf = Common.getPeers();
                for (PeerInfo p : pInf) 
                {
                    if (p.PeerId != MyPeerId)
                        dRateList.add(new PeerInfo.Pair<Integer, Integer>(p.PeerId, 0));
                }
            } 
            catch (Exception e) 
            {
                Log.Write("Exception in resetDownloadRates: " + e.getMessage());
                System.out.println("Exception in resetDownloadRates: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }
        }

        public static void setDownloadRate (Integer peer, Integer rate)
        {
            int count = 0;
            for(Pair<Integer, Integer> p : dRateList)
            {
                if (p.getLeft().intValue() == peer.intValue())
                {
                    dRateList.get(count).setRight(dRateList.get(count).getRight() + rate);                
                }
                count ++;
            }
        }
    }

    

    public static class UnchokedNeighbors 
    {
        // True = unchoked
        // False = choked
        public static List<Pair<Integer, Boolean>> unchokedNeighbors = new ArrayList<Pair<Integer, Boolean>>();
        static int iteration = 2;

        public UnchokedNeighbors() 
        {
            try
            {
                List<PeerInfo> pInf = Common.getPeers();
                for (PeerInfo p : pInf) 
                {
                    if (p.PeerId != PeerInfo.MyPeerId)
                    {
                        unchokedNeighbors.add(new Pair<Integer, Boolean>(p.PeerId, false));
                    }
                }
            } 
            catch (Exception e) 
            {
                Log.Write("Exception in UnchokedNeighbors default constructor: " + e.getMessage());
                System.out.println("Exception in UnchokedNeighbors default constructor: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }

        }

        public static void setUnchokedNeighbors(Integer[] pIds) 
        {
            pReadWriteLock.writeLock().lock();
            for (int i = 0; i < requestedArray.size() - 1; i++) 
            {
                int[] temp = requestedArray.get(i);
                if (temp[1] >= iteration && temp[0] == 1)
                {
                    temp = new int[]{0,0};                    
                }
                else if (temp[0] == 1 && temp[1] < iteration )
                {
                    temp = new int[]{temp[0],temp[1] + 1};
                }
                requestedArray.set(i, temp);
            }
            pReadWriteLock.writeLock().unlock();
            if (pIds == null)
            {
                //System.out.println("setUnchokedNeighbors pIds Null");
                return;
            }
            if (PeerInfo.MyHasFile) 
            {
                List<Pair<Integer,String>> randList = new ArrayList<>();
                //randomly select who to unchoke from those peers that are interested
                for (Pair<Integer,String> p : interestedArray) 
                {
                    if (p.getRight().equals("Interested"))
                    {
                        randList.add(p);
                    }
                }
                Integer[] rand = new Integer[Common.NumberOfPreferredNeighbors <= randList.size() ? Common.NumberOfPreferredNeighbors : randList.size()];
                for (int i = 0; i < rand.length; i++) 
                {
                    Collections.shuffle(randList);
                    rand[i] = randList.remove(0).getLeft();
                }
                pIds = rand;
            }
            
            for (int i = 0; i < unchokedNeighbors.size(); i++) 
            {
                Pair<Integer, Boolean> p = unchokedNeighbors.get(i);
                boolean modified = false;
                for (Integer id : pIds) 
                {
                    if (id != null && p.getLeft().intValue() == id.intValue()) 
                    {                         
                        unchokedNeighbors.get(i).setRight(true);
                        modified = true;                            
                    }
                }
                if (!modified) 
                {
                    unchokedNeighbors.get(i).setRight(false);
                }

            }
            
            return;
        }

        public static void setOptUnchoked(Integer pId) 
        {
            if (pId == null)
            {
                //System.out.println("setOptUnchoked pId Null");
                return;
            }
            for (int i = 0; i < unchokedNeighbors.size(); i++) 
            {
                Pair<Integer, Boolean> p = unchokedNeighbors.get(i);

                if (p.getLeft().intValue() == pId.intValue()) 
                {
                    unchokedNeighbors.get(i).setRight(true);                        
                }
            }

            return;
        }

        public static Integer getOptUnchoked() 
        {

            List<Pair<Integer,String>> randList = new ArrayList<>();
            //randomly select who to unchoke from those peers that are interested
            for (Pair<Integer,String> p : interestedArray) 
            {
                for (Pair<Integer,Boolean> pair : unchokedNeighbors) 
                {
                    if (p.getRight().equals("Interested") && pair.getLeft().intValue() ==  p.getLeft().intValue() && pair.getRight())
                    {
                        System.out.println(p.getLeft());
                        randList.add(p);
                    }
                }
            }            
            if (randList.size() > 0)
            {
                Collections.shuffle(randList);
                return randList.get(0).getLeft();
            }
            return null;

        }

    }

    public static PeerInfo getPeerInfo(String hostname) throws IOException 
    {
        List<PeerInfo> temp = Common.getPeers();
        for (PeerInfo pInfo : temp) 
        {
            String hname;
            if (pInfo.HostName.contains(".cise.ufl.edu"))
                hname = pInfo.HostName.split(".cise.ufl.edu")[0];
            else
                hname = pInfo.HostName;

            if (hname.equals(hostname)) 
            {
                if (!_init) 
                {

                    BitSet init = new BitSet(Common.Piece);
                    MyPeerId = pInfo.PeerId;
                    MyHostName = pInfo.HostName;
                    MyPortNumber = pInfo.PortNumber;
                    MyHasFile = pInfo.HasFile;
                    MyFile = new byte[Common.FileSize];

                    if (MyHasFile) 
                    {
                        Path path = Paths.get("peer_" + MyPeerId + "/" + Common.FileName);
                        MyFile = Files.readAllBytes(path);
                        init.flip(0, Common.Piece);
                    }
                    MyFileBits = init;

                }
                _init = true;
                return pInfo;
            }
        }
        // Log out that we are returning null here
        return null;
    }

    public static PeerInfo getPeerInfo(int peerid) throws IOException 
    { 
        List<PeerInfo> temp = Common.getPeers();
        for (PeerInfo pInfo: temp) 
        {
            if(pInfo.PeerId == peerid)
            {
                if (!_init)
                {
                    BitSet init = new BitSet(Common.Piece);
                    MyPeerId = pInfo.PeerId;
                    MyHostName = pInfo.HostName;
                    MyPortNumber = pInfo.PortNumber;
                    MyHasFile = pInfo.HasFile;
                    MyFile = new byte[Common.FileSize];
                    if (MyHasFile)
                    {
                        Path path = Paths.get("../" + Common.FileName);
                        MyFile = Files.readAllBytes(path);
                        init.flip(0,Common.Piece);
                    }
                    MyFileBits = init;
                }
                _init = true;
                return pInfo;
            }

        }
        //Log out that we are returning null here
        return null; 
    }

    public PeerInfo(String[] data)
    {
        if (data.length > 0)
        {
            this.PeerId = Integer.parseInt(data[0]);
            this.HostName = data[1];
            this.PortNumber = Integer.parseInt(data[2]);
            this.HasFile = Boolean.parseBoolean(data[3].equals("1") ? "true" : "false");

            BitSet init = new BitSet(Common.Piece);
            if (this.HasFile)
                init.flip(0,Common.Piece);
            this.FileBits = init;
            Pair<Integer,Boolean> pair = new Pair<Integer,Boolean>(this.PeerId,false);
            Pair<Integer,String> pair1 = new Pair<Integer,String>(this.PeerId,"NotInterested");
            Pair<Integer,Boolean> pair2 = new Pair<Integer,Boolean>(this.PeerId,false);
            hShakeArray.add(pair);
            interestedArray.add(pair1);
            HasFileArray.add(pair2);  
            if (!_init_)
            {                         
                for (int i = 0; i < Common.Piece; i++) 
                {
                int[] insert = new int[]{0,0};
                requestedArray.add(insert);
                }
                _init_ = true;
            } 
        }
        else
        {
            //Log out error here and throw exception
        }

    }

    public static void SetHandshake(int pid, boolean value)
    {
        Integer peerId = Integer.valueOf(pid);

        for(int i = 0; i< hShakeArray.size(); i ++)
        {
            Integer temp = hShakeArray.get(i).left;
            if (temp.intValue() == peerId.intValue())
            {
                Pair<Integer, Boolean> pair = new Pair<Integer, Boolean>(peerId, value);
                hShakeArray.set(i, pair);
            }
        }
    }

    public static void SetInterested(int pid, String value)
    {
        Integer peerId = Integer.valueOf(pid);

        for(int i = 0; i< interestedArray.size(); i ++)
        {
            Integer temp = interestedArray.get(i).left;
            if (temp.intValue() == peerId.intValue())
            {
                Pair<Integer, String> pair = new Pair<Integer, String>(peerId, value);
                interestedArray.set(i, pair);
            }
        }
    }

    public static void SetHaveFileArray(int pid, Boolean value)
    {
        Integer peerId = Integer.valueOf(pid);

        for(int i = 0; i< HasFileArray.size(); i ++)
        {
            Integer temp = HasFileArray.get(i).left;
            if (temp.intValue() == peerId.intValue())
            {
                Pair<Integer, Boolean> pair = new Pair<Integer, Boolean>(peerId, value);
                HasFileArray.set(i, pair);
            }
        }
    }

    public static Boolean isHandShake (Integer peerId)
    {
        for (Pair<Integer,Boolean> p : hShakeArray) 
        {
            if (p.left.intValue() == peerId.intValue())
            {
                return p.right;
            }
        }
        return false;
    }

    public static class Pair<L,R> 
    {
        
        private L left;
        private R right;

        public Pair(L left, R right) 
        {
            assert left != null;
            assert right != null;

            this.left = left;
            this.right = right;
        }

        public L getLeft() { return left; }

        public R getRight() { return right; }

        public void setRight(R set) { this.right = set; }

        public void setLeft(L set) { this.left = set; }
      
        @Override
        public int hashCode() { return left.hashCode() ^ right.hashCode(); }
      
        @Override
        public boolean equals(Object o) 
        {
          if (!(o instanceof Pair)) return false;
          @SuppressWarnings("unchecked")
          Pair<L,R> pairo = (Pair<L,R>) o;
          return this.left.equals(pairo.getLeft());
        }
      
      }
    
}

