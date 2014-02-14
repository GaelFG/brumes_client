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
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
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
import fr.gembasher.brumes.network.EntityState;
import fr.gembasher.brumes.network.WorldState;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameSessionController extends AbstractAppState implements ScreenController {

  private Main app;
  private Node rootNode;
  /** Contain all entities */
  private Node entities_node;
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
  
  private final ConcurrentLinkedQueue<WorldState> world_states_queue;
  
  public GameSessionController(ConcurrentLinkedQueue<WorldState> clientMessageQueue, SessionStartData session_start_data) {
      this.world_states_queue = clientMessageQueue;
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
    
    // entities
    entities_node = new Node("entities");
    rootNode.attachChild(entities_node);

  }
  
  @Override
  public void update(float delta) {
      app.getInputManager().setCursorVisible(false);
      process_server_intputs();
      update_positions();
      majHUD();
  }
  
  /** genere une entitée encore inexistante dans le graphe de la scène */
  private Node create_entity_from_state(EntityState entity_state) {
      Node node;
      node = new Node("" + entity_state.id);
      
      // partie graphique a refaire plus tard
      Box b = new Box(1, 1, 1);
      Geometry geom = new Geometry("Box", b);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.Red);
      geom.setMaterial(mat);
      node.attachChild(geom);
      // fin ârtie graphique
      
      node.setLocalTranslation((float)(entity_state.x), (float)(entity_state.y), 0f);
      node.setUserData("looked", new Vector3f((float)(entity_state.looked_x), (float)(entity_state.looked_y), 0f));
      node.setUserData("destination", new Vector3f((float)(entity_state.destination_x), (float)(entity_state.destination_y), 0f));
      node.lookAt((Vector3f)(node.getUserData("looked")), Vector3f.UNIT_Y);
      return node;
  }
  
  private void process_server_intputs(){
    WorldState world_state;
    while ((world_state = world_states_queue.poll()) != null) {
        if (world_state.timestamp > last_processed_world_state_timestamp) {
            last_processed_world_state_timestamp = world_state.timestamp;
            for (EntityState entity_state : world_state.entities_states) {
                Spatial entity_node = entities_node.getChild(""+entity_state.id); //TODO cast necessaire ou pas ?
                if (entity_node == null) {
                    entity_node = create_entity_from_state(entity_state);
                    entities_node.attachChild(entity_node);
                } else {
                    
                    //TODO Si la position est trop désynchronisée, faire une teleportation
                    //entity_node.setLocalTranslation((float)(entity_state.x), (float)(entity_state.y), 0f);
                   
                    entity_node.setUserData("looked", new Vector3f((float)(entity_state.looked_x), (float)(entity_state.looked_y), 0f));
                    entity_node.setUserData("destination", new Vector3f((float)(entity_state.destination_x), (float)(entity_state.destination_y), 0f));
                }
            }
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
        for (Spatial entity : entities_node.getChildren()) {
            Vector3f destination = (Vector3f)(entity.getUserData("destination"));
            Vector3f movement = destination.subtract(entity.getWorldTranslation()).normalize();
            movement = movement.mult(0.2f); //TODO reguler vitesse
            entity.move(movement);
            // le regard
            entity.lookAt((Vector3f)(entity.getUserData("looked")), Vector3f.UNIT_Y);
        }
    }
}