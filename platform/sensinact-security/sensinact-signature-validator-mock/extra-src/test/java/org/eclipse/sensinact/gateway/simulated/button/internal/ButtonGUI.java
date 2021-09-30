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
package org.eclipse.sensinact.gateway.simulated.button.internal;

import org.eclipse.sensinact.gateway.simulated.button.api.ButtonSetterItf;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

public class ButtonGUI extends JLabel implements MouseListener, ButtonSetterItf {
    private final ImageIcon buttonOn;
    private final ImageIcon buttonOff;
    private final ButtonAdapter listener;
    private boolean state;

    public ButtonGUI(Mediator mediator,ButtonAdapter listener) {
        super();
        super.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        buttonOn = new ImageIcon(mediator.getContext().getBundle().getResource("images/button/on.png"));
        buttonOff = new ImageIcon(mediator.getContext().getBundle().getResource("images/button/off.png"));
        super.setIcon(buttonOff);
        this.state = false;
        super.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        super.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        super.addMouseListener(this);
        this.listener = listener;
    }

    public void stop() {
        super.setVisible(false);
    }

    @Override
    public void move(boolean value) {
        this.listener.mouseReleased(value);
    }

    /**
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    /**
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * @see MouseListener#mouseExited(MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent arg0) {
        if (state) {
            this.move(false);
            super.setIcon(buttonOff);
            state = false;
        } else {
            this.move(true);
            super.setIcon(buttonOn);
            state = true;
        }
    }
}
