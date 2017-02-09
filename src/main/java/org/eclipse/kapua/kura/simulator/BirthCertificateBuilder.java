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

import static com.google.common.io.BaseEncoding.base16;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.stream.Collectors.joining;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.kapua.kura.simulator.app.Application;

@NonNullByDefault
public class BirthCertificateBuilder {
	private final GatewayConfiguration configuration;
	private final Instant started;
	private final Set<Application> applications;

	public BirthCertificateBuilder(final GatewayConfiguration configuration, final Instant started,
			final Set<Application> applications) {
		this.configuration = configuration;
		this.started = started;
		this.applications = applications;
	}

	@SuppressWarnings("null")
	protected static void fillInConnection(final Map<String, Object> metrics) {
		try {
			final Enumeration<@Nullable NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			if (nis == null) {
				return;
			}

			final List<String> interfaces = new LinkedList<>();
			final List<String> addresses = new LinkedList<>();

			while (nis.hasMoreElements()) {
				final NetworkInterface ni = nis.nextElement();
				if (ni == null) {
					continue;
				}

				// we only "iterate" over the first address, then we break

				for (final InterfaceAddress addr : ni.getInterfaceAddresses()) {

					String hostAddress = addr.getAddress().getHostAddress();

					// replace IPv6 interface
					hostAddress = hostAddress.replaceAll("%.*$", "");

					addresses.add(hostAddress);

					final String name = ni.getName();
					final String hwAddr = base16().upperCase().withSeparator(":", 2).encode(ni.getHardwareAddress());

					interfaces.add(String.format("%s (%s)", name, hwAddr));

					break; // only the first address
				}

				// now add the result

				metrics.put("connection_interface", String.join(",", interfaces));
				metrics.put("connection_ip", String.join(",", addresses));
			}
		} catch (final Exception e) {
		}
	}

	@SuppressWarnings("null")
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

		fillInConnection(result);

		result.put("application_ids",
				this.applications.stream().map(app -> app.getDescriptor().getId()).collect(joining(",")));

		return result;
	}
}
