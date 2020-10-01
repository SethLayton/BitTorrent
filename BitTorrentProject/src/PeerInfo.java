import java.util.BitSet;
import java.util.List;

public class PeerInfo
{
    public int PeerId;
    public String HostName;
    public int PortNumber;
    public boolean HasFile;
    public BitSet FileBits;
    public static int Pieces;
    private static boolean _init = false;
    public static int MyPeerId;
    public static String MyHostName;
    public static int MyPortNumber;
    public static boolean MyHasFile;
    public static BitSet MyFileBits;

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

    public PeerInfo(String[] data)
    {
        if (data.length > 0)
        {
            this.PeerId = Integer.parseInt(data[0]);
            this.HostName = data[1];
            this.PortNumber = Integer.parseInt(data[2]);
            this.HasFile = Boolean.parseBoolean(data[3]);

            BitSet init = new BitSet(Common.getPiece());
            if (this.HasFile)
            {
                init.flip(0,Common.getPiece()-1);
            }
            this.FileBits = init;
        }
        else
        {
            //Log out error here and throw exception
        }

    }
    
}

