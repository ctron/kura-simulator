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
package org.eclipse.kapua.kura.simulator.payload;

import org.eclipse.kapua.kura.simulator.topic.Topics;

public class Message {
	private final String localTopic;
	private final byte[] payload;

	public Message(final String localTopic, final byte[] payload) {
		this.localTopic = localTopic;
		this.payload = payload;
	}

	public String getLocalTopic() {
		return this.localTopic;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public Message localize(final String prefix) {
		final String newTopic = Topics.localize(prefix, this.localTopic);
		if (newTopic == null) {
			return null;
		}
		return new Message(newTopic, this.payload);
	}

	@Override
	public String toString() {
		return String.format("[%s -> %s]", this.localTopic, this.payload);
	}
}
