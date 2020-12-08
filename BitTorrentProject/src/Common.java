import java.util.Properties;
import java.io.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Common 
{
    //Singleton variables set on startup
    private static boolean _cinit = false;
    private static boolean _pinit = false;
    public static int NumberOfPreferredNeighbors;
    public static int UnchokingInterval;
    public static int OptimisticUnchokingInterval;
    public static String FileName;
    public static int FileSize;
    public static int PieceSize;
    public static int Piece;
    private static boolean localtest= false;

    //List of PeerInfo that is initialized from the PeerInfo.cfg on startup and then
    //Updated dynamically while running
    public static List<PeerInfo> Peers = new ArrayList<PeerInfo>();
    public static List<Integer> PeerIds = new ArrayList<Integer>();

    public static List<PeerInfo> getPeers() {loadpeerinfo(); return Peers; }
    public static Integer GetSmallestPeerId() {loadpeerinfo(); return Collections.min(PeerIds); }
    public static Integer GetLargestPeerId() {loadpeerinfo(); return Collections.max(PeerIds); }

    public static void loadproperties()
    {
        Properties comProperties = new Properties();
        try 
        {
            if (!_cinit)
            {
                localtest = java.net.InetAddress.getLocalHost().toString().split("/")[0].equals("");
                comProperties.load(new FileInputStream(localtest ? "BitTorrentProject/src/Common.cfg" : "Common.cfg"));                    
                
                NumberOfPreferredNeighbors = Integer.parseInt(comProperties.get("NumberOfPreferredNeighbors").toString());
                UnchokingInterval = Integer.parseInt(comProperties.get("UnchokingInterval").toString());
                
                OptimisticUnchokingInterval = Integer.parseInt(comProperties.get("OptimisticUnchokingInterval").toString());
                FileName = comProperties.get("FileName").toString();
                
                FileSize = Integer.parseInt(comProperties.get("FileSize").toString());
                
                PieceSize = Integer.parseInt(comProperties.get("PieceSize").toString());
                
                Piece = (int)(Math.ceil((double)FileSize/PieceSize));
                
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
                File myObj = new File(localtest ? "BitTorrentProject/src/PeerInfo.cfg" : "PeerInfo.cfg");
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

    public static byte[] concat(byte[]...arrays)
	{
		// Determine the length of the result array
		int totalLength = 0;
		for (int i = 0; i < arrays.length; i++)
		{
			totalLength += arrays[i].length;
		}

		// create the result array
		byte[] result = new byte[totalLength];

		// copy the source arrays into the result array
		int currentIndex = 0;
		for (int i = 0; i < arrays.length; i++)
		{
			System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
			currentIndex += arrays[i].length;
		}

    	return result;
    }	
    
    public static String removeBadFormat (String in)
    {
        return in.replaceAll("[^A-Za-z0-9()\\[\\]]", "");
    }
}

