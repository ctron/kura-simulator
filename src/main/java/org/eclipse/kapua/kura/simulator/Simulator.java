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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.kapua.kura.simulator.app.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class Simulator implements AutoCloseable {

	@SuppressWarnings("null")
	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	private final SimulatorTransport transport;
	private final Instant started;

	private final BirthCertificateBuilder birthCertificateBuilder;

	private final Set<Application> applications;

	@SuppressWarnings("null")
	public Simulator(final GatewayConfiguration configuration, final SimulatorTransport transport,
			final Set<Application> applications) {

		this.started = now();
		this.transport = transport;
		this.applications = new HashSet<>(applications);

		this.birthCertificateBuilder = new BirthCertificateBuilder(configuration, this.started, this.applications);

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
