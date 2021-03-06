package fi.helsinki.cs.tmc.ui;

import java.awt.Component;
import javax.swing.Box;

/**
 * Factory for {@link Box} layout helpers.
 * 
 * <p>
 * More convenient than the factory methods in Box.
 */
public class Boxer {
    
    public static Component hglue() {
        return Box.createHorizontalGlue();
    }
    
    public static Component vglue() {
        return Box.createVerticalGlue();
    }
    
    public static Component hstrut(int width) {
        return Box.createHorizontalStrut(width);
    }
    
    public static Component vstrut(int height) {
        return Box.createVerticalStrut(height);
    }
    
    public static Box hbox(Component... components) {
        Box box = Box.createHorizontalBox();
        for (Component component : components) {
            box.add(component);
        }
        return box;
    }
    
    public static Box vbox(Component... components) {
        Box box = Box.createVerticalBox();
        for (Component component : components) {
            box.add(component);
        }
        return box;
    }
}
