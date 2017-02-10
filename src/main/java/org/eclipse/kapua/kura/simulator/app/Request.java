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
package org.eclipse.kapua.kura.simulator.app;

import static java.util.Objects.requireNonNull;
import static org.eclipse.kapua.kura.simulator.payload.Metrics.KEY_REQUESTER_CLIENT_ID;
import static org.eclipse.kapua.kura.simulator.payload.Metrics.KEY_REQUEST_ID;
import static org.eclipse.kapua.kura.simulator.payload.Metrics.KEY_RESPONSE_CODE;
import static org.eclipse.kapua.kura.simulator.payload.Metrics.KEY_RESPONSE_EXCEPTION_MESSAGE;
import static org.eclipse.kapua.kura.simulator.payload.Metrics.KEY_RESPONSE_EXCEPTION_STACKTRACE;
import static org.eclipse.kapua.kura.simulator.payload.Metrics.getAsString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.kapua.kura.simulator.payload.Message;
import org.eclipse.kapua.kura.simulator.payload.Metrics;
import org.eclipse.kapua.kura.simulator.topic.Topic;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload.Builder;

public class Request {

	private final ApplicationContext applicationContext;
	private final Message message;
	private final Map<String, Object> metrics;
	private final String requestId;
	private final String requesterClientId;

	public Request(final ApplicationContext applicationContext, final Message message,
			final Map<String, Object> metrics, final String requestId, final String requesterClientId) {

		requireNonNull(applicationContext);
		requireNonNull(message);
		requireNonNull(metrics);
		requireNonNull(requestId);
		requireNonNull(requesterClientId);

		this.applicationContext = applicationContext;
		this.message = message;
		this.metrics = metrics;
		this.requestId = requestId;
		this.requesterClientId = requesterClientId;
	}

	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	public Message getMessage() {
		return this.message;
	}

	public Map<String, Object> getMetrics() {
		return Collections.unmodifiableMap(this.metrics);
	}

	public String getRequesterClientId() {
		return this.requesterClientId;
	}

	public String getRequestId() {
		return this.requestId;
	}

	public static Request parse(final ApplicationContext context, final Message message) throws Exception {

		final KuraPayload payload = KuraPayloadProto.KuraPayload.parseFrom(message.getPayload());

		final Map<String, Object> metrics = Metrics.extractMetrics(payload);

		final String requestId = getAsString(metrics, KEY_REQUEST_ID);
		if (requestId == null) {
			throw new IllegalArgumentException("Request ID (" + KEY_REQUEST_ID + ") missing in message");
		}

		final String requesterClientId = getAsString(metrics, KEY_REQUESTER_CLIENT_ID);
		if (requesterClientId == null) {
			throw new IllegalArgumentException(
					"Requester Client ID (" + KEY_REQUESTER_CLIENT_ID + ") missing in message");
		}

		return new Request(context, message, metrics, requestId, requesterClientId);
	}

	public void sendReply(final int responseCode, final Map<String, Object> metrics) {
		if (metrics.containsKey(KEY_RESPONSE_CODE)) {
			throw new IllegalStateException("Metrics must not already contain '" + KEY_RESPONSE_CODE + "'");
		}

		final Builder payload = KuraPayload.newBuilder();
		Metrics.buildMetrics(payload, metrics);
		Metrics.addMetric(payload, KEY_RESPONSE_CODE, responseCode);

		this.applicationContext.sendMessage(Topic.reply(this.requesterClientId, this.requestId),
				payload.build().toByteArray());
	}

	public void sendSuccess(final Map<String, Object> metrics) {
		sendReply(200, metrics);
	}

	public void sendError(final Throwable error) {

		final Map<String, Object> metrics = new HashMap<>();
		if (error != null) {
			metrics.put(KEY_RESPONSE_EXCEPTION_MESSAGE, ExceptionUtils.getRootCauseMessage(error));
			metrics.put(KEY_RESPONSE_EXCEPTION_STACKTRACE, ExceptionUtils.getStackTrace(error));
		}
		sendReply(500, metrics);
	}

	public void sendNotFound() {
		sendReply(404, Collections.emptyMap());
	}
}
