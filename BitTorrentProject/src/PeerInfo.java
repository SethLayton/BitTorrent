import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
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
    public boolean HasFile;
    public BitSet FileBits;
    public boolean choked;
    public boolean optUnchoked;
    public static long downloadRate;
    public boolean interested;
    public static List<Pair<Integer, Boolean>> hShakeArray = new ArrayList<Pair<Integer, Boolean>>();

    // Locally running processes information. This is set from the singleton on
    // startup
    // MyFileBits is set dynamically as the process runs
    public static int MyPeerId;
    public static String MyHostName;
    public static int MyPortNumber;
    public static boolean MyHasFile;
    public static BitSet MyFileBits;
    public static byte[] MyFile;

    // Singleton variables
    public static int Pieces;
    private static boolean _init = false;

    public static class DownloadRate {
        // This is a 'globally' accessed list. Needs to be thread safe
        public static List<Pair<Integer, Float>> dRateList = new ArrayList<Pair<Integer, Float>>();

        public DownloadRate() {
            try {
                List<PeerInfo> pInf = Common.getPeers();
                for (PeerInfo p : pInf) {
                    if (p.PeerId != MyPeerId)
                        dRateList.add(new Pair<Integer, Float>(p.PeerId, (float) 0));
                }
            } catch (Exception e) {
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

        // Returns the highest download rate currently stored in the list
        // Don't know if this is actually useful
        public static Float getHighestDownload() {
            // default instantiate this return value
            Float highDownload = dRateList.get(0).getRight();
            try {
                for (Pair<Integer, Float> p : dRateList) {
                    if (p.getRight() > highDownload)
                        highDownload = p.getRight();
                }
            } catch (Exception e) {
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

        // returns the PeerId of the top (number of preferred peers) highest
        // downloadrates
        // Since the set function resets all the not currently uploading values to 0
        // This function needs only look at elements in the list that have a download
        // rate > 0
        public static Integer[] getHighestDownloadRates() {
            // default instantiate this return value
            Integer[] highDownloads = new Integer[Common.NumberOfPreferredNeighbors + 1];
            int count = 0;
            try {
                for (Pair<Integer, Float> p : dRateList) {
                    if (p.getRight() > 0) {
                        highDownloads[count] = p.getLeft();
                        count++;
                    }
                }
            } catch (Exception e) {
                Log.Write("Exception in getHighestDownloadRates: " + e.getMessage());
                System.out.println("Exception in getHighestDownloadRates: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }

            return highDownloads;
        }

        // This needs to be updated to be thread safe
        // Pass in the currently sending (uploading to this client) peers and their
        // downloadspeeds
        // reset the rest of the list back to a 0 download rate
        public static void setDownloadRate(List<Pair<Integer, Float>> rates) {
            try {
                for (Pair<Integer, Float> p : rates) {
                    if (Common.PeerIds.contains(p.getLeft())) {
                        for (int i = 0; i < dRateList.size(); i++) {
                            Integer temp = dRateList.get(i).left;
                            if (temp.intValue() == p.getLeft().intValue()) {
                                dRateList.set(i, p);
                            } else
                                dRateList.set(i, new Pair<Integer, Float>(dRateList.get(i).getLeft(), (float) 0));
                        }
                    } else {
                        Log.Write("Exception in setDownloadRate: peerId passed in (" + p.getLeft()
                                + ") does not belong to the group of peers.");
                        System.out.println("Exception in setDownloadRate: peerId passed in (" + p.getLeft()
                                + ") does not belong to the group of peers.");
                    }
                }
            } catch (Exception e) {
                Log.Write("Exception in setDownloadRate: " + e.getMessage());
                System.out.println("Exception in setDownloadRate: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
            }
        }
    }

    public static class UnchokedNeighbors {
        // True = unchoked
        // False = choked
        public static List<Pair<Integer, Boolean>> unchokedNeighbors = new ArrayList<Pair<Integer, Boolean>>();

        public UnchokedNeighbors() {
            try {
                List<PeerInfo> pInf = Common.getPeers();
                for (PeerInfo p : pInf) {
                    if (p.PeerId != MyPeerId)
                        unchokedNeighbors.add(new Pair<Integer, Boolean>(p.PeerId, false));
                }
            } catch (Exception e) {
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

        public static void setUnchokedNeighbors(Integer[] pIds) {

            for (int i = 0; i < unchokedNeighbors.size(); i++) {
                Pair<Integer, Boolean> p = unchokedNeighbors.get(i);
                boolean modified = false;
                for (Integer id : pIds) {
                    if (p.getLeft() == id) {
                        unchokedNeighbors.get(i).setRight(true);
                        modified = true;
                    }
                }
                if (!modified) {
                    unchokedNeighbors.get(i).setRight(false);
                }

            }

            return;
        }

        public static void setOptUnchoked(Integer pId) {

            for (int i = 0; i < unchokedNeighbors.size(); i++) {
                Pair<Integer, Boolean> p = unchokedNeighbors.get(i);

                if (p.getLeft() == pId) {
                    unchokedNeighbors.get(i).setRight(true);
                }

            }

            return;
        }

        public static Integer getOptUnchoked() {
            Random r = new Random();
            boolean found = false;
            int a;
            do {
                a = r.nextInt(unchokedNeighbors.size());
                if (!unchokedNeighbors.get(a).getRight()) {
                    found = true;
                }
            } while (!found);

            return unchokedNeighbors.get(a).getLeft();

        }

    }

    public static PeerInfo getPeerInfo(String hostname) throws IOException {
        List<PeerInfo> temp = Common.getPeers();
        for (PeerInfo pInfo : temp) {
            String hname;
            if (pInfo.HostName.contains(".cise.ufl.edu"))
                hname = pInfo.HostName.split(".cise.ufl.edu")[0];
            else
                hname = pInfo.HostName;

            if (hname.equals(hostname)) {
                if (!_init) {

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
            hShakeArray.add(pair);
            
            
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
          return this.left.equals(pairo.getLeft()) &&
                 this.right.equals(pairo.getRight());
        }
      
      }
    
}

