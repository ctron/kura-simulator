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

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(final String[] args) throws Exception {

		toInfinityAndBeyond();

		logger.info("Starting ...");

		final GatewayConfiguration configuration = new GatewayConfiguration(
				"tcp://kapua-broker:kapua-password@localhost:1883", "kapua-sys", "sim-1");

		try (MqttSimulatorTransport transport = new MqttSimulatorTransport(configuration);
				Simulator simulator = new Simulator(configuration, transport);) {
			Thread.sleep(10_000);
		}
	}

	private static void toInfinityAndBeyond() {
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("org.eclipse.paho.client.mqttv3").setLevel(Level.ALL);
	}
}
