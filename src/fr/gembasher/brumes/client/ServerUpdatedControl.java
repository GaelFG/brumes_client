/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class ServerUpdatedControl extends AbstractControl implements Savable, Cloneable {

    @Override
    protected void controlUpdate(float tpf) {
        if(spatial != null) {
      // spatial.rotate(tpf,tpf,tpf); // example behaviour
         float velocity = 0.1f;
         Vector3f destination = (Vector3f)(spatial.getUserData("destination"));
         if (spatial.getWorldTranslation().distance(destination) > velocity) {
            Vector3f movement = destination.subtract(spatial.getWorldTranslation()).normalize();
            movement = movement.mult(velocity); //TODO reguler vitesse
            spatial.move(movement);
          }
          spatial.lookAt((Vector3f)(spatial.getUserData("looked")), Vector3f.UNIT_Y);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
