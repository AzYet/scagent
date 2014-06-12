package org.opendaylight.controller.scagent.service.impl;

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.controller.scagent.service.api.SCAgentSampleServiceAPI;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class SCAgentSampleServiceImpl extends AbstractBindingAwareConsumer
		implements BundleActivator, BindingAwareConsumer,
		SCAgentSampleServiceAPI {

	private ConsumerContext session;
	private ServiceRegistration<SCAgentSampleServiceAPI> registerService;

	@Override
	public String sayHello(String args) {
		
		return String.format("args = %s: %s", args, "this is a sample service");

	}
	@Override
    protected void startImpl(BundleContext context) {
		
		System.out.println("from scagent service activator: start to resgister service");
		
        registerService = context.registerService(SCAgentSampleServiceAPI.class, this, null);
        
    }

	@Override
	public void onSessionInitialized(ConsumerContext arg0) {
		
		System.out.println("from scagent service activator: osgi framework is calling me. ");

		this.session = session;

	}

}
