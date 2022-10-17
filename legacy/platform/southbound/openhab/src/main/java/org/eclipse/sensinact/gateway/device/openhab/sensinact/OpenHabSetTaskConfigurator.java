package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

public class OpenHabSetTaskConfigurator implements HttpTaskConfigurator {
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        final Object[] parameters = task.getParameters();
        final String content = parameters[1].toString();
        task.setContent(content);
    }
}