package org.eclipse.sensinact.gateway.core.remote;

public class SensinactCoreBaseIFaceManagerFactoryImpl implements SensinactCoreBaseIFaceManagerFactory  {

	private SensinactCoreBaseIFaceManager sensinactCoreBaseIFaceManager;
	
	@Override
	public SensinactCoreBaseIFaceManager instance() {
		if(this.sensinactCoreBaseIFaceManager == null) {
			this.sensinactCoreBaseIFaceManager = new SensinactCoreBaseIFaceManagerImpl();
		}
		return this.sensinactCoreBaseIFaceManager;
	}

}
