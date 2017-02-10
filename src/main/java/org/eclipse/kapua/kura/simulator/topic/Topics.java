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
package org.eclipse.kapua.kura.simulator.topic;

public final class Topics {
	private Topics() {
	}

	public static String localize(final String prefix, final String topic) {
		if (prefix == null) {
			return topic;
		}

		if (topic == null) {
			return null;
		}

		// test if this is an exact match

		if (topic.equals(prefix)) {
			// main topic
			return "";
		}

		// not a match

		if (!topic.startsWith(prefix + "/")) {
			return null;
		}

		// cut off prefix

		return topic.substring(prefix.length() + 1);
	}

}
