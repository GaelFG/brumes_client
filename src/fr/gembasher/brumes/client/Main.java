package fr.gembasher.brumes.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import de.lessvoid.nifty.Nifty;
import fr.gembasher.brumes.network.EntityDescription;
import fr.gembasher.brumes.network.KryoRegisterer;
import fr.gembasher.brumes.network.LoggedAs;
import fr.gembasher.brumes.network.WorldState;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    private LoginMenuController login_menu_controller;
    private GameSessionController game_session_controller;
    private BulletAppState bulletAppState = new BulletAppState();
    private Nifty nifty;
    
    private final Client client = new Client();
    private final ConcurrentLinkedQueue<WorldState> world_states_queue = new ConcurrentLinkedQueue<WorldState>();
    private final ConcurrentLinkedQueue<LoggedAs> loginStateQueue = new ConcurrentLinkedQueue<LoggedAs>();
    private final ConcurrentLinkedQueue<EntityDescription> entity_descriptions_queue = new ConcurrentLinkedQueue<EntityDescription>();
    
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        login_menu_controller = new LoginMenuController();
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);

	/** Initialise le thread client kryonet **/
	Log.set(Log.LEVEL_ERROR);
	client.start();
	/* Enregistrement des classes message*/
	KryoRegisterer.registerAll(client.getKryo());
            try {
		client.connect(Constantes.TIME_OUT, Constantes.IP_SERVEUR, Constantes.PORT_SERVEUR);
            } catch (IOException e) {
		System.out.println("Erreur connection serveur");
		return; // On quitte le client
            }
        /* Ajout du listener qui traite les inputs des joueurs */
	client.addListener( new NetworkListener(world_states_queue, loginStateQueue, entity_descriptions_queue) );
 
            
        goMenu();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    @Override
    public InputManager getInputManager() {
        return inputManager;
    }
    
    public void goMenu() {
        stateManager.detach(game_session_controller);
        stateManager.detach(bulletAppState);
        nifty.fromXml("Interface/Menus.xml", "login", login_menu_controller);
        game_session_controller = null;
        stateManager.attach(login_menu_controller);    
    }
    
    public void goGame( SessionStartData session_start_data ) {
        game_session_controller = new GameSessionController(client, world_states_queue, entity_descriptions_queue, session_start_data);
        stateManager.detach(login_menu_controller);
        nifty.fromXml("Interface/Hud.xml", "hud", game_session_controller);
        stateManager.attach(bulletAppState);
        stateManager.attach(game_session_controller);
        
    }
    
    public Client getClient() {
        return client;
    }
    
    public ConcurrentLinkedQueue<LoggedAs> getLoginStateQueue() {
        return loginStateQueue;
    }

public BulletAppState getBulletAppState() {
    return bulletAppState;
}

public Camera getCam() {
    return cam;
}

}
