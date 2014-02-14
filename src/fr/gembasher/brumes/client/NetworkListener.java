package fr.gembasher.brumes.client;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import fr.gembasher.brumes.network.LoggedAs;
import fr.gembasher.brumes.network.WorldState;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkListener extends Listener {
	protected ConcurrentLinkedQueue<WorldState> world_states_queue;
        protected ConcurrentLinkedQueue<LoggedAs> loginStateQueue;
	
	public NetworkListener(ConcurrentLinkedQueue<WorldState> pClientMessageQueue, ConcurrentLinkedQueue<LoggedAs> pLoginStateQueue){
		world_states_queue = pClientMessageQueue;
                loginStateQueue = pLoginStateQueue;
	}
	
        @Override
	   public void received (Connection con, Object msg) {
	   
            if ( msg instanceof WorldState ) {
                  world_states_queue.add((WorldState)msg);
	      }
	   
           else if ( msg instanceof LoggedAs ) {
                  loginStateQueue.add((LoggedAs)msg);
	      }
	   }
	   
        @Override
	   public void disconnected(Connection c) {
		   System.out.println("T'es deco lol");
	   }
}
