package fr.gembasher.brumes.client;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import de.lessvoid.nifty.Nifty;


/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    private LoginMenuController monCtrlMenu;
    private GameSessionController monCtrlHud;
    private Nifty nifty;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
       // On crée les deux appStates
        monCtrlMenu = new LoginMenuController();
        monCtrlHud = new GameSessionController();
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        
        //
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
    
    public void goMenu() {
        stateManager.detach(monCtrlHud);
        nifty.fromXml("Interface/Menus.xml", "login", monCtrlMenu);
        stateManager.attach(monCtrlMenu);
        
        
    }
    
    public void goGame() {
        stateManager.detach(monCtrlMenu);
        nifty.fromXml("Interface/Hud.xml", "hud", monCtrlHud);
        stateManager.attach(monCtrlHud);
    }
}
