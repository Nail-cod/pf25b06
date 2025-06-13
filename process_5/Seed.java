package process_5;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

public enum Seed {   // to save as "Seed.java"
    CROSS("X", "images/cross.gif"),   // displayName, imageFilename
    NOUGHT("O", "images/not.gif"),
    NO_SEED(" ", null);

    // Private variables
    private String displayName;
    private Image img = null;

    // Constructor (must be private)
    private Seed(String name, String imageFilename) {
        this.displayName = name;

        if (imageFilename != null) {
            URL imgURL = getClass().getClassLoader().getResource(imageFilename);
            ImageIcon icon = null;
            if (imgURL != null) {
                icon = new ImageIcon(imgURL);
                //System.out.println(icon);  // debugging
            } else {
                System.err.println("Couldn't find file " + imageFilename);
            }
            img = icon.getImage();
        }
    }

    // Public getters
    public String getDisplayName() {
        return displayName;
    }
    public Image getImage() {
        return img;
    }
}