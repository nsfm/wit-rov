package com.witrov.main;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class HelpPanel extends JPanel{

    private BufferedImage image;

    public HelpPanel() {
       try {                
          image = ImageIO.read(new File(System.getProperty("user.home")+"/AppData/Roaming/WIT_ROV/help.png"));
       } catch (IOException ex) {
            ex.printStackTrace();
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
    }

}