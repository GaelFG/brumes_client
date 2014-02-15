/*
 * A renommer, et traiter la velocité en attribut d'instance plutot qu'en var temporaire
 * les entitées aevc ce controle se deplacent a chaque frame vers leur destination en fonction de leur velocité
 */
package fr.gembasher.brumes.client;

import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Gaël
 */
public class ServerUpdatedControl extends AbstractControl {
    private float velocity;
    
    public ServerUpdatedControl(){}
 
    public ServerUpdatedControl(float velocity){ 
      this.velocity = velocity;
    } 
    
    @Override
    protected void controlUpdate(float tpf) {
        if(spatial != null) {
         Vector3f destination = (Vector3f)(spatial.getUserData("destination"));
         if (spatial.getWorldTranslation().distance(destination) > velocity) {
            Vector3f movement = destination.subtract(spatial.getWorldTranslation()).normalize();
            movement = movement.mult(velocity*tpf); //TODO reguler vitesse
            spatial.move(movement);
          }
          spatial.lookAt((Vector3f)(spatial.getUserData("looked")), Vector3f.UNIT_Y);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
