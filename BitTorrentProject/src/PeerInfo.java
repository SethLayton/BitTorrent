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

    public static PeerInfo getPeerInfo(String hostname) 
    { 
        List<PeerInfo> temp = Common.getPeers();
        for (PeerInfo pInfo: temp) 
        {
            if(pInfo.HostName.equals(hostname))
            {
                if (!_init)
                {
                    
                    BitSet init = new BitSet(Common.Piece);
                    MyPeerId = pInfo.PeerId;
                    MyHostName = pInfo.HostName;
                    MyPortNumber = pInfo.PortNumber;
                    MyHasFile = pInfo.HasFile;
                    
                    if (MyHasFile)
                    {
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
    
    public static PeerInfo getPeerInfo(int peerid) 
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
                    if (MyHasFile)
                    {
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
            hShakeArray.add(pair);
            
            
        }
        else
        {
            //Log out error here and throw exception
        }

    }

    public static void SetHandshake(int pid)
    {
        Integer peerId = Integer.valueOf(pid);

        for(int i = 0; i< hShakeArray.size(); i ++)
        {
            Integer temp = hShakeArray.get(i).left;
            if (temp.intValue() == peerId.intValue())
            {
                Pair<Integer, Boolean> pair = new Pair<Integer, Boolean>(peerId, true);
                hShakeArray.set(i, pair);
            }
        }
    }

    public static Boolean isHandShake (Integer peerId)
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
          return this.left.equals(pairo.getLeft()) &&
                 this.right.equals(pairo.getRight());
        }
      
      }
    
}

