package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.UriUtils;

public class OpenHabActTaskConfigurator implements HttpTaskConfigurator {
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        final String leaf = UriUtils.getLeaf(task.getPath());
        final String content = leaf.substring(5).toUpperCase();
        task.setContent(content);
    }
}