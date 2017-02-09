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

public interface SimulatorTransport {

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

	public void sendBirthCertificate(Map<String, Object> birthMetrics);

}
