import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import java.lang.Object;

public class PeerInfo
{
    //Information about ALL peers. This is updated dynamically while running
    public int PeerId;
    public String HostName;
    public int PortNumber;
    public boolean HasFile;
    public BitSet FileBits;
    public boolean choked;
    public boolean optUnchoked;
    public long downloadRate;
    public boolean interested;
    public BitSet fileBits;
    public static List<Pair<Integer,Boolean>> hShakeArray = new ArrayList<Pair<Integer,Boolean>>();
     

    //Locally running processes information. This is set from the singleton on startup
    //MyFileBits is set dynamically as the process runs
    public static int MyPeerId;
    public static String MyHostName;
    public static int MyPortNumber;
    public static boolean MyHasFile;
    public static BitSet MyFileBits;

    //Singleton variables
    public static int Pieces;
    private static boolean _init = false;

    //Getter functions (need to update to use static variables so that we dont need these getters.)
    public int getPeerId() { return PeerId; }
    public String getHostName() { return HostName; }
    public int getPortNumber() { return PortNumber; }
    public boolean getHasFile() { return HasFile; }

    public static PeerInfo getPeerInfo(String hostname) 
    { 
        List<PeerInfo> temp = Common.getPeerInfo();
        for (PeerInfo pInfo: temp) 
        {
            if(pInfo.HostName.equals(hostname))
            {
                if (!_init)
                {
                    
                    BitSet init = new BitSet(Common.getPiece());
                    MyPeerId = pInfo.PeerId;
                    MyHostName = pInfo.HostName;
                    MyPortNumber = pInfo.PortNumber;
                    MyHasFile = pInfo.HasFile;
                    if (MyHasFile)
                    {
                        init.flip(0,Common.getPiece()-1);
                    }
                    MyFileBits = init;
                }
            
                return pInfo;
            }
        }
        //Log out that we are returning null here
        return null; 
    }
    
    public static PeerInfo getPeerInfo(int peerid) 
    { 
        List<PeerInfo> temp = Common.getPeerInfo();
        for (PeerInfo pInfo: temp) 
        {
            if(pInfo.PeerId == peerid)
            {
                if (!_init)
                {
                    BitSet init = new BitSet(Common.getPiece());
                    MyPeerId = pInfo.PeerId;
                    MyHostName = pInfo.HostName;
                    MyPortNumber = pInfo.PortNumber;
                    MyHasFile = pInfo.HasFile;
                    if (MyHasFile)
                    {
                        init.flip(0,Common.getPiece()-1);
                    }
                    MyFileBits = init;
                }
            
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

            BitSet init = new BitSet(Common.getPiece());
            if (this.HasFile)
                init.flip(0,Common.getPiece());
            this.FileBits = init;
        }
        else
        {
            //Log out error here and throw exception
        }

    }

    public static void SetHandshake(int pid)
    {
        Integer peerId = Integer.valueOf(pid);

        for (Pair<Integer,Boolean> p : hShakeArray) 
        {
            if (p.left == peerId)
            {
                p.right = true;
            }
        }
    }

    public Boolean isHandShake (Integer peerId)
    {
        for (Pair<Integer,Boolean> p : hShakeArray) 
        {
            if (p.left == peerId)
            {
                return p.right;
            }
        }
        return false;
    }

    public class Pair<L,R> 
    {
        private final L left;
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
      
        @Override
        public int hashCode() { return left.hashCode() ^ right.hashCode(); }
      
        @Override
        public boolean equals(Object o) 
        {
          if (!(o instanceof Pair)) return false;
          Pair pairo = (Pair) o;
          return this.left.equals(pairo.getLeft()) &&
                 this.right.equals(pairo.getRight());
        }
      
      }
    
}

