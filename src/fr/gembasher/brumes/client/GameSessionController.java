package fr.gembasher.brumes.client;

import com.esotericsoftware.kryonet.Client;
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
import fr.gembasher.brumes.network.EntityDescription;
import fr.gembasher.brumes.network.EntityDescriptionRequest;
import fr.gembasher.brumes.network.EntityState;
import fr.gembasher.brumes.network.WorldState;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameSessionController extends AbstractAppState implements ScreenController {
  private Client client;
  private Main app;
  private Node rootNode;
  private Node entities_node;
  private AppStateManager stateManager;
  private Nifty nifty;
  private Screen screen;
  private Calendar cal;
  private Player player;
  private TerrainQuad terrain;
  private Material mat_terrain;
  private long last_processed_world_state_timestamp;
  private AssetManager assetManager;
  private final ConcurrentLinkedQueue<WorldState> world_states_queue;
  private final ConcurrentLinkedQueue<EntityDescription> entity_descriptions_queue;
  
  public GameSessionController(Client client, ConcurrentLinkedQueue<WorldState> clientMessageQueue, ConcurrentLinkedQueue<EntityDescription> entity_descriptions_queue, SessionStartData session_start_data) {
      this.client = client;
      this.world_states_queue = clientMessageQueue;
      this.entity_descriptions_queue = entity_descriptions_queue;
      cal = Calendar.getInstance();
      last_processed_world_state_timestamp = 0;
      player = new Player(100, 100);
      player.setNom(session_start_data.player_char_name);
      player.entity_id = session_start_data.player_entity_id;
  }
    
  @Override
  public void initialize(AppStateManager stateManager, Application pApp) {
    this.app = (Main)pApp;
    this.stateManager = stateManager;
    this.assetManager = app.getAssetManager();
    rootNode = this.app.getRootNode();
    rootNode.detachAllChildren(); // On nettoie la scène graphique
    app.getFlyByCamera().setEnabled(true);
    //skybox
    rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/BrightSky.dds", false));
    mettreEnPlaceTerrain();
    entities_node = new Node("entities");
    rootNode.attachChild(entities_node);
  }
  
  @Override
  public void update(float delta) {
      app.getInputManager().setCursorVisible(false);
      process_server_intputs();
      majHUD();
  }
  
  private void redefine_entity_from_description(Node redefined_node, EntityDescription description) {
      redefined_node.setUserData("display_name", description.display_name);
      redefined_node.detachChildNamed("Model");
      
      //TODO recuperer un modele en fonction du nom du modele
      Box cyl = new Box(0.5f, 1f, 0.5f);
      Geometry geom = new Geometry("Model", cyl);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      switch(description.model_parameters) {
          case "color=red":
              mat.setColor("Color", ColorRGBA.Red); break;
          case "color=blue":
              mat.setColor("Color", ColorRGBA.Yellow); break;
          case "color=yellow":
              mat.setColor("Color", ColorRGBA.Blue); break;
          default:
              mat.setColor("Color", ColorRGBA.Green);
      }
      
      geom.setMaterial(mat);
      redefined_node.attachChild(geom);
      
  }
  
  /** genere une entitée encore inexistante dans le graphe de la scène */
  private Node create_entity_from_state(EntityState entity_state) {
      Node node;
      node = new Node("" + entity_state.id);
      
      // partie graphique a refaire plus tard
      Box b = new Box(0.1f, 0.1f, 0.1f);
      Geometry geom = new Geometry("Model", b);
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.Red);
      geom.setMaterial(mat);
      node.attachChild(geom);
      // fin ârtie graphique
      
      node.setLocalTranslation((float)(entity_state.x), 0f, (float)(entity_state.y));
      node.setUserData("looked", new Vector3f((float)(entity_state.looked_x), 0f, (float)(entity_state.looked_y)));
      node.setUserData("destination", new Vector3f((float)(entity_state.destination_x), 0f, (float)(entity_state.destination_y)));
      node.lookAt((Vector3f)(node.getUserData("looked")), Vector3f.UNIT_Y);
      
      //controls
      ServerUpdatedControl server_updated_control;
      server_updated_control = new ServerUpdatedControl(1f);
      node.addControl(server_updated_control);
      
      return node;
  }
  
  //TODO gerer arrivée des events, gerer desynchronisation des positions des modeles
  private void process_server_intputs(){
    WorldState world_state;
    EntityDescription entity_description;
    
    while ((entity_description = entity_descriptions_queue.poll()) != null) {
        Spatial entity_node = entities_node.getChild(""+entity_description.id);
        redefine_entity_from_description((Node)entity_node, entity_description);
        
    }
    
    while ((world_state = world_states_queue.poll()) != null) {
        if (world_state.timestamp > last_processed_world_state_timestamp) {
            last_processed_world_state_timestamp = world_state.timestamp;
            for (EntityState entity_state : world_state.entities_states) {
                if (entity_state.id != player.entity_id) {
                    Spatial entity_node = entities_node.getChild(""+entity_state.id); //TODO cast necessaire ou pas ?
                    if (entity_node == null) {
                        client.sendTCP(new EntityDescriptionRequest(entity_state.id));
                        entity_node = create_entity_from_state(entity_state);
                        entities_node.attachChild(entity_node);
                    } else {
                        //TODO Si la position est trop désynchronisée, faire une teleportation
                        //entity_node.setLocalTranslation((float)(entity_state.x), (float)(entity_state.y), 0f);
                        entity_node.setUserData("looked", new Vector3f((float)(entity_state.looked_x), 0f, (float)(entity_state.looked_y)));
                        entity_node.setUserData("destination", new Vector3f((float)(entity_state.destination_x), 0f, (float)(entity_state.destination_y)));
                    }
                }
            }
        }
    }
  }

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
    terrain.setLocalTranslation(0, -4, 0);
    terrain.setLocalScale(1f, 0.025f, 1f);
    rootNode.attachChild(terrain);
 
    /** 5. The LOD (level of detail) depends on were the camera is: */
    TerrainLodControl control = new TerrainLodControl(terrain, app.getCamera());
    terrain.addControl(control);
  }
  
  public Player getPlayer() {
      return player;
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
}