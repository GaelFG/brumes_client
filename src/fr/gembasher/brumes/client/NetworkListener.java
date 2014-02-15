package fr.gembasher.brumes.client;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import fr.gembasher.brumes.network.EntityDescription;
import fr.gembasher.brumes.network.LoggedAs;
import fr.gembasher.brumes.network.WorldState;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkListener extends Listener {
	private ConcurrentLinkedQueue<WorldState> world_states_queue;
        private ConcurrentLinkedQueue<LoggedAs> loginStateQueue;
        private ConcurrentLinkedQueue<EntityDescription> entity_descriptions_queue;
	
	public NetworkListener(ConcurrentLinkedQueue<WorldState> pClientMessageQueue, ConcurrentLinkedQueue<LoggedAs> pLoginStateQueue, ConcurrentLinkedQueue<EntityDescription> pEntityDescriptionQueue){
		world_states_queue = pClientMessageQueue;
                loginStateQueue = pLoginStateQueue;
                entity_descriptions_queue = pEntityDescriptionQueue;
	}
	
        @Override
	   public void received (Connection con, Object msg) {
	   
            if ( msg instanceof WorldState ) {
                  world_states_queue.add((WorldState)msg);
	      }
	   
           else if ( msg instanceof LoggedAs ) {
                  loginStateQueue.add((LoggedAs)msg);
	      }
            
            else if ( msg instanceof EntityDescription ) {
                  entity_descriptions_queue.add((EntityDescription)msg);
	      }
            
	   }
	   
        @Override
	   public void disconnected(Connection c) {
		   System.out.println("T'es deco lol");
	   }
}
