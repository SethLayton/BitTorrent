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
            _logPath = "Log_peer_{0}.log"; 
        String ret = MessageFormat.format(_logPath, String.valueOf(PeerInfo.MyPeerId), LocalDate.now());
        return ret;
    }
    
    public static void Write(String msg)
    {
        if (_logPath == null)
        {
            LogPath();
        }
        //System.out.println("logging: " + msg);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(LogPath()), true)))
        {            
            String timeStamp = LocalTime.now().toString();
            writer.write(timeStamp + ": " + msg.replace(",","") + "\n");
            writer.flush();
            
            //System.out.println("logged");
        }
        catch(Exception e)
        {
            //Log out an error with logging somewhere
        }
    }  
}
