/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.simulated.light.swing;

import org.eclipse.sensinact.gateway.simulated.light.internal.LightConfigListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

/**
 * The light JPanel.
 */
public class LightPanel extends JPanel implements LightConfigListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * The light frame.
     */
    private JFrame jFrame;
    private static final int NUM_IMAGES = 11;
    ImageIcon[] images = new ImageIcon[NUM_IMAGES];
    JLabel picture;

    public LightPanel() {
        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame.setTitle("light");
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // Create the label that displays the light picture
        picture = new JLabel();
        picture.setHorizontalAlignment(JLabel.CENTER);
        picture.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        picture.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        brightnessChanged(0);
        add(picture);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Creates the GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public void brightnessChanged(int imageNumber) {
        if (images[imageNumber] == null) {
            images[imageNumber] = createImageIcon("/images/light/light" + imageNumber + ".png");
        }
        if (images[imageNumber] != null) {
            picture.setIcon(images[imageNumber]);
            picture.updateUI();

        } else {
            picture.setText("image #" + imageNumber + " not found");
            picture.updateUI();
        }
    }

    protected ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = LightPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        System.out.println("Couldn't find file: " + path);
        return null;
    }

    private void createAndShowGUI() {
        jFrame.add(this, BorderLayout.CENTER);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void stop() {
        jFrame.setVisible(false);
        jFrame.remove(this);
        jFrame.dispose();
    }
}
