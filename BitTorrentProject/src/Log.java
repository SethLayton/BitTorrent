public class Log 
{
    //     private static String FILENAME_DT_FORMAT = "yyyy-MM-dd";
    //     private static String _logPath = null;
    //     private static String logPath; 

    //     public static String getLogPath()
    //     { 
    //         if (_logPath == null) _logPath = Env.localTesting ? @"C:\Temp\VelocityService.{0}.log" : ConfigurationManager.AppSettings["LogPath"]; 
    //         return string.Format(_logPath, DateTime.Now.ToString(FILENAME_DT_FORMAT)); 
    //     }
        
    //     public static void Write(string msg, bool optionalOutput = false)
    //     {
    //         try
    //         {
    //             if (CommonFunctions.GetAppConfigValue("DEBUG") != "TRUE" && optionalOutput)
    //             {
    //                 return;
    //             }
    //             for (int i = 0; i < NumberOfRetries; ++i)
    //             {
    //                 try
    //                 {
    //                     using (StreamWriter writer = File.AppendText(!optionalOutput ? logPath : logPath2))
    //                     {
                            
    //                         string timeStamp = DateTime.Now.ToString(CommonDefs.DATETIME_FORMAT);
    //                         writer.WriteLine(timeStamp + " " + msg.Replace(Environment.NewLine, Environment.NewLine + "\t"));
    //                         writer.Flush();
    //                     }
    //                     break;
    //                 }
    //                 catch(IOException)
    //                 {
    //                     if (i == NumberOfRetries) 
    //                         throw;

    //                     Thread.Sleep(DelayOnRetry);
    //                 }
    //             }
    //         }
    //         catch(Exception e)
    //         {
    //             CommonFunctions.SendEMail("Exception in " + System.Reflection.MethodBase.GetCurrentMethod().Name, e.Message + Environment.NewLine + e.StackTrace);
    //         }
    //     }

    //     public static void WriteIntegration(string msg)
    //     {
    //         try
    //         {
    //             for (int i = 0; i < NumberOfRetries; ++i)
    //             {
    //                 try
    //                 {
    //                     using (StreamWriter writer = File.AppendText(integrationLogPath))
    //                     {
    //                         string timeStamp = DateTime.Now.ToString(CommonDefs.DATETIME_FORMAT);
    //                         writer.WriteLine(timeStamp + " " + msg.Replace(Environment.NewLine, Environment.NewLine + "\t"));
    //                         writer.Flush();
    //                     }
    //                     break;
    //                 }
    //                 catch (IOException)
    //                 {
    //                     if (i == NumberOfRetries)
    //                         throw;

    //                     Thread.Sleep(DelayOnRetry);
    //                 }
    //             }
    //         }
    //         catch(Exception e)
    //         {
    //             CommonFunctions.SendEMail("Exception in " + System.Reflection.MethodBase.GetCurrentMethod().Name, e.Message + Environment.NewLine + e.StackTrace); 
    //         }
    //     }
    // }
}
