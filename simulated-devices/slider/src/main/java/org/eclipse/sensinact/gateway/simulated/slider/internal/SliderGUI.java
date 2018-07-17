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
package org.eclipse.sensinact.gateway.simulated.slider.internal;

import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;

@SuppressWarnings("serial")
public class SliderGUI extends JFrame implements MouseListener, SliderSetterItf {
    private final SliderAdapter listener;

    public SliderGUI(SliderAdapter listener) {
        this.listener = listener;
        super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // Creates the GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public void stop() {
        super.setVisible(false);
        super.dispose();
    }

    Long lastValueTimeStamp = 0l;

    /**
     * Creates the GUI.
     */
    private void createAndShowGUI() {
        super.setTitle("slider");
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        //Create the slider
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
        slider.addMouseListener(this);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Long currentValueTimeStamp = Calendar.getInstance().getTime().getTime();
                if (currentValueTimeStamp - lastValueTimeStamp > 500) {
                    lastValueTimeStamp = currentValueTimeStamp;
                    JSlider slider = ((JSlider) e.getSource());
                    SliderGUI.this.listener.mouseReleased(slider.getValue());
                }
            }
        });
        slider.setMajorTickSpacing(100);
        slider.setMinorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        slider.setFont(font);
        sliderPanel.add(slider);
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        super.add(sliderPanel, BorderLayout.CENTER);
        super.setSize(550, 100);
        super.setVisible(true);
    }

    public void move(int i) {
        this.listener.mouseReleased(i);
    }

    /**
     * @InheritedDoc
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    /**
     * @InheritedDoc
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * @InheritedDoc
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * @InheritedDoc
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent event) {
    }

    /**
     * @InheritedDoc
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent event) {
        this.move(((JSlider) event.getSource()).getValue());
    }
}
