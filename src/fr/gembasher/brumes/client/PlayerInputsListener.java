/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.gembasher.brumes.client;

import com.esotericsoftware.kryonet.Client;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;

/**
 *
 * @author Gaël
 */
public class PlayerInputsListener implements AnalogListener, ActionListener{
    private Client client;
    private Player player;
    
    public PlayerInputsListener(Client client, Player player) {
        this.client = client;
        this.player = player;
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {

    }

    @Override
    public void onAction(String binding, boolean isPressed, float tpf) {
        switch (binding) {
            case "Left":
                 player.left = isPressed;
                 break;
            case "Right":
                player.right= isPressed;
                break;
            case "Up":
                player.up = isPressed;
                break;
            case "Down":
                player.down = isPressed;
                break;
            case "Jump":
                break;
        }
  }
    
}
