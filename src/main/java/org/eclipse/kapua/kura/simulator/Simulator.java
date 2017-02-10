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

import static java.time.Instant.now;

import java.time.Instant;
import java.util.Set;

import org.eclipse.kapua.kura.simulator.app.Application;
import org.eclipse.kapua.kura.simulator.app.ApplicationController;
import org.eclipse.kapua.kura.simulator.payload.BirthCertificateBuilder;
import org.eclipse.kapua.kura.simulator.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulator implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	private final Transport transport;
	private final Instant started;

	private final BirthCertificateBuilder birthCertificateBuilder;

	private final ApplicationController applicationController;

	public Simulator(final GatewayConfiguration configuration, final Transport transport,
			final Set<Application> applications) {

		this.started = now();
		this.transport = transport;

		// set of callbacks

		this.transport.whenConnected(this::connected);
		this.transport.whenDisconnected(this::disconnected);

		// set up application controller

		this.applicationController = new ApplicationController(transport);
		applications.forEach(this.applicationController::add);

		// set up builder

		this.birthCertificateBuilder = new BirthCertificateBuilder(configuration, this.started,
				this.applicationController::getApplicationIds);

		// finally connect

		this.transport.connect();
	}

	@Override
	public void close() {
		this.transport.disconnect();
	}

	public void connected() {
		logger.info("Connected ... sending birth certificate ...");
		// FIXME: pull out as simulator modules
		this.transport.sendMessage(Topic.device("MQTT/BIRTH"), this.birthCertificateBuilder.build());
		this.applicationController.connected();
	}

	public void disconnected() {
		this.applicationController.disconnected();
	}

}