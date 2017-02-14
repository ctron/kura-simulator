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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.eclipse.kapua.kura.simulator.app.Application;
import org.eclipse.kapua.kura.simulator.app.annotated.AnnotatedApplication;
import org.eclipse.kapua.kura.simulator.app.command.SimpleCommandApplication;
import org.eclipse.kapua.kura.simulator.app.deploy.SimpleDeployApplication;
import org.eclipse.kapua.kura.simulator.util.NameThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class TestApplication {
	private static final Logger logger = LoggerFactory.getLogger(TestApplication.class);

	public static void main(final String[] args) throws Throwable {

		toInfinityAndBeyond();

		final Options opts = new Options();
		opts.addOption("n", "basename", true, "The base name of the simulator instance");
		opts.addOption("c", "count", true, "The number of instances to start");
		opts.addOption("b", "broker", true, "The URL to the broker");
		opts.addOption("a", "account-name", true, "The name of the account");
		opts.addOption("s", "shutdown", true, "Shutdown simulator after x seconds");

		final CommandLine cli = new DefaultParser().parse(opts, args);

		final String basename = cli.getOptionValue('n', "sim-");
		final int count = Integer.parseInt(cli.getOptionValue('c', "1"));
		final String broker = cli.getOptionValue('b', "tcp://kapua-broker:kapua-password@localhost:1883");
		final String accountName = cli.getOptionValue('a', "kapua-sys");
		final long shutdownAfter = Long.parseLong(cli.getOptionValue('s', Long.toString(Long.MAX_VALUE / 1_000L)));

		logger.info("Starting ...");

		logger.info("\tbasename : {}", basename);
		logger.info("\tcount: {}", count);
		logger.info("\tbroker: {}", broker);
		logger.info("\taccount-name: {}", accountName);

		final ScheduledExecutorService downloadExecutor = Executors
				.newSingleThreadScheduledExecutor(new NameThreadFactory("DownloadSimulator"));

		final List<AutoCloseable> close = new LinkedList<>();

		try {
			for (int i = 1; i <= count; i++) {

				final String name = String.format("%s%s", basename, i);

				final GatewayConfiguration configuration = new GatewayConfiguration(broker, accountName, name);

				final Set<Application> apps = new HashSet<>();
				apps.add(new SimpleCommandApplication(s -> String.format("Command '%s' not found", s)));
				apps.add(AnnotatedApplication.build(new SimpleDeployApplication(downloadExecutor)));

				final MqttSimulatorTransport transport = new MqttSimulatorTransport(configuration);
				close.add(transport);
				final Simulator simulator = new Simulator(configuration, transport, apps);
				close.add(simulator);
			}

			Thread.sleep(shutdownAfter * 1_000L);
			logger.info("Bye bye...");
		} finally {
			downloadExecutor.shutdown();
			closeAll(close);
		}

		logger.info("Exiting...");
	}

	private static void closeAll(final List<AutoCloseable> close) throws Throwable {
		final LinkedList<Throwable> errors = new LinkedList<>();

		for (final AutoCloseable c : close) {
			try {
				c.close();
			} catch (final Exception e) {
				errors.add(e);
			}
		}

		final Throwable e = errors.pollFirst();
		if (e != null) {
			errors.forEach(e::addSuppressed);
			throw e;
		}
	}

	/**
	 * Redirect Paho logging to SLF4J
	 */
	private static void toInfinityAndBeyond() {
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("org.eclipse.paho.client.mqttv3").setLevel(Level.ALL);
	}
}
