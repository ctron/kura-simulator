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
package org.eclipse.kapua.kura.simulator.app.deploy;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;

public class SimpleDeployApplication extends AbstractDeployApplication {

	public static class BundleState {
		private final String symbolicName;
		private final String version;
		private int state;

		public BundleState(final String symbolicName, final String version, final int state) {
			this.symbolicName = symbolicName;
			this.version = version;
			this.state = state;
		}

		public int getState() {
			return this.state;
		}

		public void setState(final int state) {
			this.state = state;
		}

		public String getSymbolicName() {
			return this.symbolicName;
		}

		public String getVersion() {
			return this.version;
		}
	}

	private final Map<Long, BundleState> bundles = new TreeMap<>();

	public SimpleDeployApplication() {
		this.bundles.put(0L, new BundleState("org.osgi", "6.0.0", Bundle.ACTIVE));
		this.bundles.put(1L, new BundleState("org.eclipse.kura.api", "2.1.0", Bundle.ACTIVE));
		this.bundles.put(2L, new BundleState("org.eclipse.kura.core", "2.1.1", Bundle.ACTIVE));
		this.bundles.put(3L, new BundleState("org.eclipse.kura.unresolved", "2.1.2", Bundle.INSTALLED));
		this.bundles.put(4L, new BundleState("org.eclipse.kura.unstarted", "2.1.1", Bundle.RESOLVED));
	}

	public SimpleDeployApplication(final Map<Long, BundleState> bundles) {
		if (bundles != null) {
			this.bundles.putAll(bundles);
		}
	}

	@Override
	protected List<BundleInformation> getBundles() {
		return this.bundles.entrySet()
				.stream().map(entry -> new BundleInformation(entry.getValue().getSymbolicName(),
						entry.getValue().getVersion(), entry.getKey(), entry.getValue().getState()))
				.collect(Collectors.toList());
	}

	@Override
	protected boolean startBundle(final long bundleId) {
		final BundleState bundle = this.bundles.get(bundleId);
		if (bundle == null) {
			return false;
		}

		if (bundle.getState() == Bundle.RESOLVED) {
			bundle.setState(Bundle.ACTIVE);
		}

		return true;
	}

	@Override
	protected boolean stopBundle(final long bundleId) {
		final BundleState bundle = this.bundles.get(bundleId);
		if (bundle == null) {
			return false;
		}

		if (bundle.getState() == Bundle.ACTIVE) {
			bundle.setState(Bundle.RESOLVED);
		}

		return true;
	}

}
