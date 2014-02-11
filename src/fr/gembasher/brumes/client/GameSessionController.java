package fr.gembasher.brumes.client;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import fr.gembasher.brumes.network.WorldState;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameSessionController extends AbstractAppState implements ScreenController {

  private Main app;
  private Node rootNode;
  private AppStateManager stateManager;
  private Nifty nifty;
  private Screen screen;
  
  private Calendar cal = Calendar.getInstance();
  
  //PLayerStat
  private Player player;
  private TerrainQuad terrain;
  private Material mat_terrain;
  
  private long last_processed_world_state_timestamp = 0;
  
  private AssetManager assetManager;
  
  private final ConcurrentLinkedQueue<WorldState> clientMessageQueue;
  
  public GameSessionController(ConcurrentLinkedQueue<WorldState> clientMessageQueue, SessionStartData session_start_data) {
      this.clientMessageQueue = clientMessageQueue;
      player = new Player(100, 100);
      player.setNom(session_start_data.player_char_name);
  }
  
  public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
  }
  
    public void onStartScreen() {
  }

    public void onEndScreen() {
  }
    
  @Override
  public void initialize(AppStateManager stateManager, Application pApp) {
    this.app = (Main)pApp;
    this.stateManager = stateManager;
    this.assetManager = app.getAssetManager();
    rootNode = this.app.getRootNode();
    
    //
    rootNode.detachAllChildren(); // On nettoie la scène graphique
    app.getFlyByCamera().setEnabled(true);
    
    //skybox
    rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/BrightSky.dds", false));
    
    //terrain
    mettreEnPlaceTerrain();
    
    Box b = new Box(1, 1, 1);
    Geometry geom = new Geometry("Box", b);

    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.Blue);
    geom.setMaterial(mat);
    rootNode.attachChild(geom);
        
  }
  
  @Override
  public void update(float delta) {
      app.getInputManager().setCursorVisible(false);
      process_server_intputs();
      update_positions();
      majHUD();
  }
  
  private void process_server_intputs(){
    WorldState world_state;
    while ((world_state = clientMessageQueue.poll()) != null) {
        if (world_state.timestamp > last_processed_world_state_timestamp) {
            last_processed_world_state_timestamp = world_state.timestamp;
            // calculer liste des entites affichées
            // parcourir la liste des EntityUpdate
                // Si un node existe avec le meme id que l'update
                    // mettre a jour les infos
                    //si la position actuelle de l'entiée est abherente par rapporte a celle du serveur
                        // la position devient immediatement celle du serveur
                    //finsi
                //sinon
                    // créer le node (demander info supplementaire serveur ? plus tard)
                //finsi
                // marquer les entites mises a jour
            // fin parcours
            // si des entitées affichées ne sont pas mentionnées dans le world state, masquer leur node
        }
        //dans tout les cas prendre en comptes les autres evenement
    }
  }
  
  public void goMainMenu(){
      
  }
  
  public void goOptions() {
      
  }
  
  public Player getPlayer() {
      return player;
  }
  
  /**
   * Met à jour les variables du hud
   */
  public void majHUD() {
    Element niftyElement = nifty.getCurrentScreen().findElementByName("nomperso");
    niftyElement.getRenderer(TextRenderer.class).setText(player.getNom());
    
    niftyElement = nifty.getCurrentScreen().findElementByName("killcounter");
    niftyElement.getRenderer(TextRenderer.class).setText("Tués : "+player.getKillCounter());
    
    niftyElement = nifty.getCurrentScreen().findElementByName("horloge");
    niftyElement.getRenderer(TextRenderer.class).setText(cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE));
    
    niftyElement = nifty.getCurrentScreen().findElementByName("vie");
    niftyElement.getRenderer(TextRenderer.class).setText(""+player.getHp());
    
    niftyElement = nifty.getCurrentScreen().findElementByName("mana");
    niftyElement.getRenderer(TextRenderer.class).setText(""+player.getMana());
  }
  
  /**
   * Crée le terrain du niveau
   */
  public void mettreEnPlaceTerrain() {
      /** 1. Create terrain material and load four textures into it. */
    mat_terrain = new Material(assetManager, 
            "Common/MatDefs/Terrain/Terrain.j3md");
 
    /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
    mat_terrain.setTexture("Alpha", assetManager.loadTexture(
            "Textures/Terrain/splat/map1_alpha.png"));
 
    /** 1.2) Add GRASS texture into the red layer (Tex1). */
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    mat_terrain.setTexture("Tex1", grass);
    mat_terrain.setFloat("Tex1Scale", 64f);
 
    /** 1.3) Add DIRT texture into the green layer (Tex2) */
    Texture dirt = assetManager.loadTexture(
            "Textures/Terrain/splat/dirt.jpg");
    dirt.setWrap(WrapMode.Repeat);
    mat_terrain.setTexture("Tex2", dirt);
    mat_terrain.setFloat("Tex2Scale", 32f);
 
    /** 1.4) Add ROAD texture into the blue layer (Tex3) */
    Texture rock = assetManager.loadTexture(
            "Textures/Terrain/splat/road.jpg");
    rock.setWrap(WrapMode.Repeat);
    mat_terrain.setTexture("Tex3", rock);
    mat_terrain.setFloat("Tex3Scale", 128f);
 
    /** 2. Create the height map */
    AbstractHeightMap heightmap = null;
    Texture heightMapImage = assetManager.loadTexture(
            "Textures/Terrain/splat/map1_height.png");
    heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
    heightmap.load();
 
    /** 3. We have prepared material and heightmap. 
     * Now we create the actual terrain:
     * 3.1) Create a TerrainQuad and name it "my terrain".
     * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
     * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
     * 3.4) As LOD step scale we supply Vector3f(1,1,1).
     * 3.5) We supply the prepared heightmap itself.
     */
    int patchSize = 65;
    terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
 
    /** 4. We give the terrain its material, position & scale it, and attach it. */
    terrain.setMaterial(mat_terrain);
    terrain.setLocalTranslation(0, -30, 0);
    terrain.setLocalScale(1f, 0.1f, 1f);
    rootNode.attachChild(terrain);
 
    /** 5. The LOD (level of detail) depends on were the camera is: */
    TerrainLodControl control = new TerrainLodControl(terrain, app.getCamera());
    terrain.addControl(control);
  }

    /* met a jour les positions des entitées en focntion de leur destination **/
    private void update_positions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}