/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.gembasher.brumes.client;

import com.esotericsoftware.kryonet.Client;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import fr.gembasher.brumes.network.PlayerIntent;

/**
 *
 * @author Gaël
 */
public class PlayerInputsListener implements AnalogListener, ActionListener{
    private Client client;
    
    public PlayerInputsListener(Client client) {
        this.client = client;
    }
    
    public void onAnalog(String name, float value, float tpf) {

    }

    public void onAction(String binding, boolean isPressed, float tpf) {
    if (binding.equals("Left")) {
      boolean left = isPressed;
      PlayerIntent player_intent = new PlayerIntent(-12.0, 0.0, 15.0, 16.0);
      client.sendTCP(player_intent);
        System.out.println("envoi player intent");
    } else if (binding.equals("Right")) {
      boolean right= isPressed;
      PlayerIntent player_intent = new PlayerIntent(12.0, 0.0, 15.0, 16.0);
      client.sendTCP(player_intent);
    } else if (binding.equals("Up")) {
      boolean up = isPressed;
    } else if (binding.equals("Down")) {
      boolean down = isPressed;
    } else if (binding.equals("Jump")) {
      //if (isPressed) { player.jump(); }
    }
  }
    
}
