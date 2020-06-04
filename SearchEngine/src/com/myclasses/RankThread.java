package com.myclasses;
import java.sql.SQLException;

public class RankThread implements Runnable{
	
	private RelvenceRank ranker;
	
	public RankThread(RelvenceRank ranker) {
		this.ranker = ranker;
	}

	@Override
	public void run() {	
		while(true) {
			try {
				ranker.rankTf(2);
				Thread.sleep(5*60*1000);
			} catch (NumberFormatException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}