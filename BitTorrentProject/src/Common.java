import java.util.Properties;
import java.io.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Common 
{
    private static boolean localtest= false;
    private static boolean _cinit = false;
    private static boolean _pinit = false;
    private static String NumberOfPreferredNeighbors;
    private static int UnchokingInterval;
    private static int OptimisticUnchokingInterval;
    private static String FileName;
    private static Long FileSize;
    private static Long PieceSize;
    private static List<PeerInfo> Peers = new ArrayList<PeerInfo>();
    private static List<Integer> PeerIds = new ArrayList<Integer>();

    public static String getNumberOfPreferredNeighbors() { loadproperties(); return NumberOfPreferredNeighbors; }
    public static int getUnchokingInterval() { loadproperties(); return UnchokingInterval; }
    public static int getOptimisticUnchokingInterval() { loadproperties(); return OptimisticUnchokingInterval; }
    public static String getFileName() { loadproperties(); return FileName; }
    public static Long getFileSize() { loadproperties(); return FileSize; }
    public static Long getPieceSize() { loadproperties(); return PieceSize; }
    public static List<PeerInfo> getPeerInfo() {loadpeerinfo(); return Peers; }
    public static Integer GetSmallestPeerId() {loadpeerinfo(); return Collections.min(PeerIds); }

    public static void loadproperties()
    {
        Properties comProperties = new Properties();
        try 
        {
            if (!_cinit)
            {
                
                localtest = java.net.InetAddress.getLocalHost().toString().split("/")[0].equals("DESKTOP-5N80JFQ");
                comProperties.load(new FileInputStream(localtest ? "BitTorrentProject/Common.cfg" : "../Common.cfg"));                    
                NumberOfPreferredNeighbors = comProperties.get("NumberOfPreferredNeighbors").toString();
                UnchokingInterval = Integer.parseInt(comProperties.get("UnchokingInterval").toString());
                OptimisticUnchokingInterval = Integer.parseInt(comProperties.get("OptimisticUnchokingInterval").toString());
                FileName = comProperties.get("FileName").toString();
                FileSize = Long.parseLong(comProperties.get("FileSize").toString());
                PieceSize = Long.parseLong(comProperties.get("PieceSize").toString());
                
                _cinit = true;
            }   
            
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private static void loadpeerinfo()
    {
        try 
        {
            loadproperties();
            if (!_pinit)
            {
                //String localMachine = java.net.InetAddress.getLocalHost().toString().split("/")[0]; 
                File myObj = new File(localtest ? "BitTorrentProject/PeerInfo.cfg" : "../PeerInfo.cfg");
                Scanner myReader = new Scanner(myObj);
                //int listid = 0;
                while (myReader.hasNextLine()) 
                {
                    String[] data = myReader.nextLine().split(" ");
                    Peers.add(new PeerInfo(data));
                    PeerIds.add(Integer.parseInt(data[0]));
                }
                myReader.close();

                _pinit = true;
            }
        } 
        catch (FileNotFoundException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

