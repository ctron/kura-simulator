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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulator implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	private final SimulatorTransport transport;
	private final Instant started;

	private final BirthCertificateBuilder birthCertificateBuilder;

	public Simulator(final GatewayConfiguration configuration, final SimulatorTransport transport) {
		this.started = now();
		this.transport = transport;

		this.birthCertificateBuilder = new BirthCertificateBuilder(configuration, this.started);

		this.transport.whenConnected(this::connected);
		this.transport.whenDisconnected(this::disconnected);

		this.transport.connect();
	}

	@Override
	public void close() {
	}

	public void connected() {
		logger.info("Connected ... sending birth certificate ...");
		this.transport.sendBirthCertificate(this.birthCertificateBuilder.build());
	}

	public void disconnected() {
	}

}
