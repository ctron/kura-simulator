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

import static java.time.Duration.between;
import static java.time.Instant.now;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class BirthCertificateBuilder {
	private final GatewayConfiguration configuration;
	private final Instant started;

	public BirthCertificateBuilder(final GatewayConfiguration configuration, final Instant started) {
		this.configuration = configuration;
		this.started = started;
	}

	public Map<String, Object> build() {
		final Map<String, Object> result = new HashMap<>();

		// all data values must be strings

		final String id = this.configuration.getClientId();

		result.put("uptime", Long.toString(between(this.started, now()).toMillis()));

		result.put("display_name", "Kura Simulator (Display Name)");
		result.put("model_name", "Kura Simulator (Model Name)");
		result.put("model_id", "kura-simulator-" + id);
		result.put("part_number", "ksim-part-123456-" + id);
		result.put("serial_number", "ksim-serial-123456-" + id);
		result.put("available_processors", "1");
		result.put("total_memory", "640");
		result.put("firmware_version", "fw.v42");
		result.put("bios_version", "bios.v42");
		result.put("os", "Kura Simulator (OS)");
		result.put("os_version", "ksim-os-v42");
		result.put("os_arch", "ksim-arch");
		result.put("jvm_name", "Kura Simulator (Java)");
		result.put("jvm_version", "ksim-java-v42");
		result.put("jvm_profile", "Kura Simulator (Java Profile)");
		result.put("kura_version", "ksim-kura-v42");
		result.put("osgi_framework", "Kura Simulator (OSGi version)");
		result.put("osgi_framework_version", "ksim-osgi-v42");

		return result;
	}
}
