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

import static org.eclipse.kapua.kura.simulator.util.Documents.create;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.kapua.kura.simulator.app.Request;
import org.eclipse.kapua.kura.simulator.app.annotated.Application;
import org.eclipse.kapua.kura.simulator.app.annotated.EXECUTE;
import org.eclipse.kapua.kura.simulator.app.annotated.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Application("DEPLOY-V2")
public abstract class AbstractDeployApplication {

	@Resource
	public void getBundles(final Request request) throws Exception {
		final List<BundleInformation> bundles = getBundles();
		request.sendSuccess(toXml(bundles).getBytes(StandardCharsets.UTF_8));
	}

	public void executeOnBundle(final Request request, final Function<Long, Boolean> consumer) {
		final long bundleId = Long.parseLong(request.renderTopic(2));
		if (consumer.apply(bundleId)) {
			request.sendSuccess(Collections.emptyMap());
		} else {
			request.sendNotFound();
		}
	}

	@EXECUTE
	public void start(final Request request) {
		executeOnBundle(request, this::startBundle);
	}

	@EXECUTE
	public void stop(final Request request) {
		executeOnBundle(request, this::stopBundle);
	}

	protected abstract boolean startBundle(long bundleId);

	protected abstract boolean stopBundle(long bundleId);

	protected abstract List<BundleInformation> getBundles();

	protected String toXml(final List<BundleInformation> bundles) throws Exception {
		return create(doc -> fillDocument(doc, bundles));
	}

	protected void fillDocument(final Document doc, final List<BundleInformation> bundles) {
		final Element bs = doc.createElement("bundles");
		doc.appendChild(bs);

		for (final BundleInformation bi : bundles) {
			final Element b = doc.createElement("bundle");
			bs.appendChild(b);
			addValue(b, "name", bi.getSymbolicName());
			addValue(b, "version", bi.getVersion());
			addValue(b, "id", Long.toString(bi.getId()));
			addValue(b, "state", bi.getStateString());
		}
	}

	protected static void addValue(final Element b, final String name, final String value) {
		final Element ele = b.getOwnerDocument().createElement(name);
		b.appendChild(ele);
		ele.setTextContent(value);
	}

}
