/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.simulated.fan.swing;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.simulated.fan.internal.FanConfigListener;
import org.w3c.dom.Node;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URL;

/**
 * The light JPanel.
 */
public class FanPanel extends JPanel implements FanConfigListener {
    private static final int NUM_IMAGES = 11;
    /**
     * The fan frame.
     */
    private JFrame jFrame;
    private ImageIcon[] images = new ImageIcon[NUM_IMAGES];
    private Mediator mediator;
    private JLabel picture;

    public FanPanel(Mediator mediator) throws IOException {
        super();
        this.mediator = mediator;
        images[0] = readImgFromFile("images/fan/fan0.gif", 0);

        for (int i = 1; i < NUM_IMAGES; i++) {
            images[i] = readImgFromFile("images/fan/fan1.gif", (i * 10));
        }

        jFrame = new JFrame("Simulated Fan");
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.setTitle("fan");
        // Create the label that displays the light picture
        picture = new JLabel();
        picture.setHorizontalAlignment(JLabel.CENTER);
        picture.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        picture.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        add(picture);
        // Creates the GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private ImageIcon readImgFromFile(String filename, int delay) {
        URL file = mediator.getContext().getBundle().getResource(filename);
        // Fix for bug when delay is 0
        try {
            // Get GIF reader
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            // Give it the stream to decode from
            reader.setInput(ImageIO.createImageInputStream(file.openStream()));
            int numImages = reader.getNumImages(true);
            // Get 'metaFormatName'. Need first frame for that.
            IIOMetadata imageMetaData = reader.getImageMetadata(0);
            String metaFormatName = imageMetaData.getNativeMetadataFormatName();
            // Prepare streams for image encoding
            ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baoStream);
            // Get GIF writer that's compatible with reader
            ImageWriter writer = ImageIO.getImageWriter(reader);
            // Give it the stream to encode to
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < numImages; i++) {
                // Get input image
                BufferedImage frameIn = reader.read(i);
                // Get input metadata
                IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree(metaFormatName);
                if (delay > 0) {
                    // Find GraphicControlExtension node
                    int nNodes = root.getLength();
                    for (int j = 0; j < nNodes; j++) {
                        Node node = root.item(j);
                        if (node.getNodeName().equalsIgnoreCase("GraphicControlExtension")) {
                            ((IIOMetadataNode) node).setAttribute("delayTime", Integer.toString(25 - (23 * delay / 100)));
                        }
                    }
                }
                // Create output metadata
                IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(frameIn), null);
                // Copy metadata to output metadata
                metadata.setFromTree(metadata.getNativeMetadataFormatName(), root);
                // Create output image
                IIOImage frameOut = new IIOImage(frameIn, null, metadata);
                // Encode output image
                writer.writeToSequence(frameOut, writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
            ios.seek(0);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            while (true) {
                try {
                    bos.write(ios.readByte());

                } catch (EOFException e) {
                    break;
                } catch (IOException e) {
                    break;
                }
            }
            // Create image using encoded data
            Image image = Toolkit.getDefaultToolkit().createImage(bos.toByteArray());
            //return  base64Encode(bos.toByteArray());
            // Trigger lazy loading of image
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(image, 0);
            try {
                mt.waitForAll();
            } catch (InterruptedException e) {
                image = null;
            }
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(104, 104);
    }

    /**
     * @InheritedDoc
     * @see FanConfigListener#speedChanged(int)
     */
    public void speedChanged(int imageNumber) {
        if (images[imageNumber] != null) {
            picture.setIcon(images[imageNumber]);
            picture.updateUI();

        } else {
            picture.setText("image #" + imageNumber + " not found");
            picture.updateUI();
        }
    }

    private void createAndShowGUI() {
        jFrame.getContentPane().add(this, BorderLayout.CENTER);
        jFrame.pack();
        jFrame.setVisible(true);
        speedChanged(0);
    }

    public void stop() {
        jFrame.setVisible(false);
        jFrame.remove(this);
        jFrame.dispose();
    }
}
