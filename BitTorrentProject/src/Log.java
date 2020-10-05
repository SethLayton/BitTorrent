import java.text.MessageFormat;
import java.time.LocalDate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalTime;

public class Log 
{
    private static String _logPath = null;

    public static String LogPath()
    { 
        if (_logPath == null) 
            _logPath = "BitTorrentProject/peer_{0}_{1}.log"; 
        String ret = MessageFormat.format(_logPath, String.valueOf(PeerInfo.MyPeerId), LocalDate.now());
        return ret;
    }
    
    public static void Write(String msg)
    {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(LogPath()), true)))
        {
            
            String timeStamp = LocalTime.now().toString();
            writer.write(timeStamp + " " + msg.replace(System.lineSeparator(), System.lineSeparator() + "\t"));
            writer.flush();
        }
        catch(Exception e)
        {
            //Log out an error with logging somewhere
        }
    }  
}
