import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;


public class ChokingInterval {
	public int numPeers;
	public int numPreferredPeers;
	public boolean[] chokedPeers;
	public int optUnchokedPeerIndex;
	public int chokingInterval;
	public int optUnchokedInterval;

	public ChokingInterval(int numPreferredPeers,int numPeers, int chokingInterval, int optUnchokedInterval)
	{
		this.numPeers = numPeers;
		this.numPrefferedPeers = numPrefferedPeers;
		this.chokedPeers = new boolean[numPeers];
		this.chokingInterval = chokingInterval;
		this.optUnchokedInterval = optUnchokedInterval;
		Semaphore sem = new Semaphore(1, true);


		Timer timer = new Timer();
		TimerTask unchokeTask = new Unchoke(sem);
		TimerTask optUnchoke = new OptUnchoke(sem);
		timer.schedule(unchokeTask, 0, this.chokingInterval * 1000);
		timer.schedule(optUnchoke, 0, this.optUnchokedInterval * 1000);

	}

	class OptUnchoke extends TimerTask 
	{
		public Semaphore sem;
		public Random rand = new Random();

		public OptUnchoke(Semaphore s) {
			this.sem = s;
		}

		public void run() 
		{
			try
			{
				sem.acquire();
				Integer pId = PeerInfo.UnchokedNeighbors.getOptUnchoked();
				setOptUnchoked(pId);
				
			} 
			catch(Exception e)
			{
				Log.Write("Exception in OptUnchoke TImerTask: " + e.getMessage());
                System.out.println("Exception in OptUnchoke TimerTask: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
			}
			finally
			{
				sem.realease();
			}
			
		}
		
	}

	class Unchoke extends TimerTask
	{
		public Semaphore sem;

		public Unchoke(Semaphore s) {
			this.sem = s;
		}
		public void run()
		{

			Integer[] highestRates = PeerInfo.downloadRate.getHighestDownloadRates();
			try 
			{
				sem.acquire();
				PeerInfo.UnchokedNeighbors.setUnchokedNeighbors(highestRates);


			}
			catch(Exception e)
			{
				Log.Write("Exception in Unchoke Timer Task: " + e.getMessage());
                System.out.println("Exception in Unchoke TimerTask: " + e.getMessage());
                System.err.println("Error: " + e.getMessage() + "\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                System.out.println(sStackTrace);
			}
			finally
			{
				sem.realease();
			}


		}
	}

}