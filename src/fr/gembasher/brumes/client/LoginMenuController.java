package fr.gembasher.brumes.client;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import fr.gembasher.brumes.network.LoggedAs;
import fr.gembasher.brumes.network.LoginRequest;

public class LoginMenuController extends AbstractAppState implements ScreenController {

  private Main app;
  private AppStateManager stateManager;
  private Nifty nifty;
  private Screen screen;
  private Node rootNode;
  private AssetManager assetManager;
  
  public LoginMenuController() {
      //TODO load login from file
  }
  
  @Override
  public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
  }
  
  @Override
    public void onStartScreen() {
        
  }

  @Override
    public void onEndScreen() {
  }
    
  @Override
  public void initialize(AppStateManager stateManager, Application pApp) {
    this.app = (Main)pApp;
    this.stateManager = stateManager;
    this.assetManager = app.getAssetManager();
    
    String login_preset = Config.read_property("login");
    if (login_preset != null) {
        nifty.getCurrentScreen().findNiftyControl("loginField", TextField.class).setText(Config.read_property("login"));
    }
    
    app.getFlyByCamera().setEnabled(false);
    app.getFlyByCamera().setMoveSpeed(100);
    rootNode = this.app.getRootNode();
    rootNode.detachAllChildren(); // On nettoie la scène graphique
    
        /** Uses Texture from jme3-test-data library! */
        ParticleEmitter fireEffect = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect.setMaterial(fireMat);
        fireEffect.setImagesX(2); fireEffect.setImagesY(2); // 2x2 texture animation
        fireEffect.setEndColor( new ColorRGBA(0f, 0.2f, 0.3f, 0.5f) );   // red
        fireEffect.setStartColor( new ColorRGBA(0f, 0.3f, 0.2f, 0.8f) ); // yellow
        fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect.setStartSize(0.1f);
        fireEffect.setEndSize(0.025f);
        fireEffect.setGravity(0f,0f,0f);
        fireEffect.setLowLife(0.5f);
        fireEffect.setHighLife(3f);
        fireEffect.getParticleInfluencer().setVelocityVariation(0.3f);
        rootNode.attachChild(fireEffect);
        /////////////////
        fireEffect.move(1, 0, 0);
  }
  
  public void quit(){
      app.stop();
  }
  
  @SuppressWarnings("SleepWhileInLoop")
  public void startSession() {
      String login;
      String password;
      
      login = nifty.getCurrentScreen().findNiftyControl("loginField", TextField.class).getText();
      password = nifty.getCurrentScreen().findNiftyControl("passwordField", TextField.class).getText();
      
      app.getClient().sendTCP(new LoginRequest(login, password));
      boolean logged = false;
      boolean rejected = false;
      Object server_response;
      
      int waited_time = 0;
      
      while(!logged && !rejected && waited_time < 2000) {
          server_response = app.getLoginStateQueue().poll();
          if (server_response !=  null) {
            if (server_response.getClass() == LoggedAs.class) {
                SessionStartData session_start_data = new SessionStartData(((LoggedAs)server_response).character_name, ((LoggedAs)server_response).character_entity_id);
                System.out.println("logged !!!");
                Config.write_property("login", login);
                app.goGame(session_start_data);
            }
          }
          waited_time += 10;
          try {
              Thread.sleep(10);
          } catch (InterruptedException ex) {}
      }
  }
}