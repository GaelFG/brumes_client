/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.gembasher.brumes.client;

/**
 *
 * @author Gaël
 */
public class SessionStartData {
    public String player_char_name;
    public String player_entity_id;
    
    public SessionStartData(String player_char_name, String player_entity_id) {
        this.player_char_name = player_char_name;
        this.player_entity_id = player_entity_id;
    }
}
