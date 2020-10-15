import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


pulic class ChokingInterval {
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

		Timer timer = new Timer();
		TimerTask unchokeTask = new Unchoke();
		TimerTask optUnchoke = new OptUnchoke();
		timer.schedule(unchokeTask, 0, this.chokingInterval * 1000);
		timer.schedule(optUnchoke, 0, this.optUnchokedInterval * 1000);

	}

	class OptUnchoke extends TimerTask 
	{
		public Random rand = new Random();

		public void run() 
		{
			if (numPrefferedPeers < numPeers)
			{
				boolean foundOptUnchoked = false;
				do {
					int ind = rand.nextInt(numPeers);
					//This logic needs to be updated when we make a decision on
					// where the datastructure is placed
					if (chokedPeers[ind])
					{
						foundOptUnchoked = true;
						this.optUnchokedPeerIndex= ind;
					}
				} while(!foundOptUnchoked)

			}
		}
		
	}

	class Unchoke extends TimerTask
	{
		public void run()
		{
			calculateUnchoked();
			

		}
		public int[] calculateUnchoked() {

			return Null;
		}
	}

}