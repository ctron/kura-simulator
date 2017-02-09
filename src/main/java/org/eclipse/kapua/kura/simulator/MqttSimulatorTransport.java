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

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload.KuraMetric;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto.KuraPayload.KuraMetric.ValueType;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

public class MqttSimulatorTransport implements AutoCloseable, SimulatorTransport {

	private static final Logger logger = LoggerFactory.getLogger(MqttSimulatorTransport.class);

	private final GatewayConfiguration configuration;

	private final MqttAsyncClient client;

	private final MqttConnectOptions connectOptions;

	private Runnable onConnected;

	private Runnable onDisconnected;

	public MqttSimulatorTransport(final GatewayConfiguration configuration) throws MqttException {
		this.configuration = configuration;

		final String plainBrokerUrl = plainUrl(configuration.getBrokerUrl());
		this.client = new MqttAsyncClient(plainBrokerUrl, configuration.getClientId());
		this.connectOptions = createConnectOptions(configuration.getBrokerUrl());
	}

	private static String plainUrl(final String brokerUrl) {
		try {
			final URIBuilder u = new URIBuilder(brokerUrl);
			u.setUserInfo(null);
			return u.build().toString();
		} catch (final URISyntaxException e) {
			throw new RuntimeException("Failed to clean up broker URL", e);
		}
	}

	private MqttConnectOptions createConnectOptions(final String brokerUrl) {
		try {
			final URIBuilder u = new URIBuilder(brokerUrl);
			final MqttConnectOptions result = new MqttConnectOptions();

			final String ui = u.getUserInfo();
			if (ui != null && !ui.isEmpty()) {
				final String[] toks = ui.split("\\:", 2);
				if (toks.length == 2) {
					result.setUserName(toks[0]);
					result.setPassword(toks[1].toCharArray());
				}
			}

			return result;
		} catch (final URISyntaxException e) {
			throw new RuntimeException("Failed to create MQTT options", e);

		}
	}

	@Override
	public void connect() {
		try {
			this.client.connect(this.connectOptions, null, new IMqttActionListener() {

				@Override
				public void onSuccess(final IMqttToken asyncActionToken) {
					handleConnected();
				}

				@Override
				public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
					logger.warn("Failed to connect", exception);
				}
			});
		} catch (final MqttException e) {
			logger.warn("Failed to initiate connect", e);
		}
	}

	@Override
	public void disconnect() {
		try {
			this.client.disconnect(null, new IMqttActionListener() {

				@Override
				public void onSuccess(final IMqttToken asyncActionToken) {
					handleDisconnected();
				}

				@Override
				public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
					logger.warn("Failed to disconnect", exception);
				}
			});
		} catch (final MqttException e) {
			logger.warn("Failed to initiatate disconnect", e);
		}
	}

	@Override
	public void close() throws MqttException {
		try {
			this.client.disconnectForcibly();
		} finally {
			this.client.close();
		}
	}

	@Override
	public void whenConnected(final Runnable runnable) {
		this.onConnected = runnable;
	}

	@Override
	public void whenDisconnected(final Runnable runnable) {
		this.onDisconnected = runnable;
	}

	protected void handleConnected() {
		final Runnable runnable = this.onConnected;
		if (runnable != null) {
			runnable.run();
		}
	}

	protected void handleDisconnected() {
		final Runnable runnable = this.onDisconnected;
		if (runnable != null) {
			runnable.run();
		}
	}

	@Override
	public void sendBirthCertificate(final Map<String, Object> birthMetrics) {
		try {
			final String topic = makeTopic("MQTT/BIRTH");
			final KuraPayload.Builder builder = KuraPayload.newBuilder();

			convertMetrics(builder, birthMetrics);

			this.client.publish(topic, builder.build().toByteArray(), 0, false);
		} catch (final Exception e) {
			logger.warn("Failed to send out message", e);
		}
	}

	private void convertMetrics(final KuraPayload.Builder builder, final Map<String, Object> metrics) {

		for (final Map.Entry<String, Object> metric : metrics.entrySet()) {

			final KuraMetric.Builder b = KuraMetric.newBuilder();
			b.setName(metric.getKey());

			final Object value = metric.getValue();
			if (value instanceof Boolean) {
				b.setType(ValueType.BOOL);
				b.setBoolValue((boolean) value);
			} else if (value instanceof Integer) {
				b.setType(ValueType.INT32);
				b.setIntValue((int) value);
			} else if (value instanceof String) {
				b.setType(ValueType.STRING);
				b.setStringValue((String) value);
			} else if (value instanceof Long) {
				b.setType(ValueType.INT64);
				b.setLongValue((Long) value);
			} else if (value instanceof Double) {
				b.setType(ValueType.DOUBLE);
				b.setDoubleValue((Double) value);
			} else if (value instanceof Float) {
				b.setType(ValueType.FLOAT);
				b.setFloatValue((Float) value);
			} else if (value instanceof byte[]) {
				b.setType(ValueType.BYTES);
				b.setBytesValue(ByteString.copyFrom((byte[]) value));
			} else {
				throw new IllegalArgumentException(String.format("Illegal metric data type: %s", value.getClass()));
			}

			builder.addMetric(b);
		}
	}

	private String makeTopic(final String localTopic) {
		return String.format("$EDC/%s/%s/%s", this.configuration.getAccountName(), this.configuration.getClientId(),
				localTopic);
	}
}
