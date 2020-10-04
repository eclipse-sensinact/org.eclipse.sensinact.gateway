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
package org.eclipse.sensinact.gateway.simulated.billboard.swing;

import org.eclipse.sensinact.gateway.simulated.billboard.internal.BillboardConfigListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

public class BillboardPanel extends JPanel implements BillboardConfigListener {
    protected JLabel text;
    protected JFrame jFrame;
    private final static String newline = "\n";

    public BillboardPanel() {
        jFrame = new JFrame("sensiNact Billboard");
        jFrame.setTitle("billboard");
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // Create the label that displays the light picture
        text = new JLabel();
        text.setHorizontalAlignment(JLabel.CENTER);
        text.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        text.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        messageChanged("Hello sensiNact");
        add(text);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Creates the GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    @Override
    public void messageChanged(String message) {
        text.setText(message + newline);
        text.updateUI();
    }

    public void createAndShowGUI() {
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
