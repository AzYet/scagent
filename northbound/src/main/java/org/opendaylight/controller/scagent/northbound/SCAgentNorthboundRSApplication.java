package org.opendaylight.controller.scagent.northbound;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class SCAgentNorthboundRSApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(SCAgentNorthbound.class);
		return classes;
	}
}
