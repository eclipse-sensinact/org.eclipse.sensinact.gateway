package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;

import org.osgi.framework.BundleContext;

import java.util.Collections;

public abstract class GenericActivator extends AbstractActivator<Mediator> {

    protected LocalProtocolStackEndpoint endPoint;

    @Override
    public void doStart() throws Exception {
        ExtModelConfiguration<?> configuration = ExtModelConfigurationBuilder.instance(
                mediator, getPacketClass()
        ).withServiceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
        ).withResourceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
        ).withStartAtInitializationTime(true
        ).build(this.getClass().getName()+".xml", Collections.emptyMap());
        endPoint = getEndPoint();
        this.connect(configuration);
    }

    @Override
    public void doStop() {
        try {
            endPoint.stop();
        } finally {
            endPoint = null;
        }
    }

    /**
     * @param configuration the configuration for the bridge
     * @throws InvalidProtocolStackException
     */
    protected void connect( ExtModelConfiguration<?> configuration) throws InvalidProtocolStackException {
        endPoint.connect(configuration);

    }

    public LocalProtocolStackEndpoint getEndPoint() {
        return endPoint;
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }

    public abstract Class getPacketClass();

}
