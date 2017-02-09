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

import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

public class SimpleCommandApplication extends AbstractCommandApplication {

	private final Function<String, String> handler;

	public SimpleCommandApplication(final Function<String, String> handler) {
		this.handler = handler;
	}

	@Override
	public @Nullable String executeCommand(final @Nullable String command) {
		return this.handler.apply(command);
	}

}
