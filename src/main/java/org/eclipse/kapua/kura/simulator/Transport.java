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

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.kapua.kura.simulator.payload.Message;
import org.eclipse.kapua.kura.simulator.payload.Metrics;
import org.eclipse.kapua.kura.simulator.topic.Topic;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload.Builder;

public interface Transport {

	/**
	 * Connect
	 */
	public void connect();

	/**
	 * Disconnect gracefully <br>
	 * A later call to {@link #connect()} must be possible.
	 */
	public void disconnect();

	public void whenConnected(Runnable runnable);

	public void whenDisconnected(Runnable runnable);

	public void subscribe(Topic localTopic, Consumer<Message> consumer);

	public void unsubscribe(Topic localTopic);

	public void sendMessage(Topic topic, byte[] payload);

	public default void sendMessage(final Topic topic, final Map<String, Object> metrics) {
		final Builder payload = KuraPayload.newBuilder();
		Metrics.buildMetrics(payload, metrics);
		sendMessage(topic, payload.build().toByteArray());
	}
}
