/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.kura.simulator;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.kapua.kura.simulator.app.Application;
import org.eclipse.kapua.kura.simulator.app.annotated.AnnotatedApplication;
import org.eclipse.kapua.kura.simulator.app.command.SimpleCommandApplication;
import org.eclipse.kapua.kura.simulator.app.deploy.SimpleDeployApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class TestApplication {
	private static final Logger logger = LoggerFactory.getLogger(TestApplication.class);

	public static void main(final String[] args) throws Exception {

		toInfinityAndBeyond();

		logger.info("Starting ...");

		final GatewayConfiguration configuration = new GatewayConfiguration(
				"tcp://kapua-broker:kapua-password@localhost:1883", "kapua-sys", "sim-1");

		final Set<@NonNull Application> apps = new HashSet<>();
		apps.add(new SimpleCommandApplication(s -> String.format("Command '%s' not found", s)));
		apps.add(AnnotatedApplication.build(SimpleDeployApplication.class));

		try (final MqttSimulatorTransport transport = new MqttSimulatorTransport(configuration);
				final Simulator simulator = new Simulator(configuration, transport, apps);) {
			Thread.sleep(120_000);
		}
	}

	private static void toInfinityAndBeyond() {
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("org.eclipse.paho.client.mqttv3").setLevel(Level.ALL);
	}
}
