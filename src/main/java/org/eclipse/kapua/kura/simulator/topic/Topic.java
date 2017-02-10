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

import static org.eclipse.kapua.kura.simulator.topic.Topic.Segment.account;
import static org.eclipse.kapua.kura.simulator.topic.Topic.Segment.clientId;
import static org.eclipse.kapua.kura.simulator.topic.Topic.Segment.control;
import static org.eclipse.kapua.kura.simulator.topic.Topic.Segment.plain;
import static org.eclipse.kapua.kura.simulator.topic.Topic.Segment.raw;
import static org.eclipse.kapua.kura.simulator.topic.Topic.Segment.replace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Topic {

	private static final Segment CONTROL = new Segment() {

		@Override
		public String render(final Map<String, String> context) {
			return "$EDC";
		}
	};

	private static final Segment WILDCARD = new Segment() {

		@Override
		public String render(final Map<String, String> context) {
			return "#";
		}
	};

	public interface Segment {
		public String render(Map<String, String> context);

		public static Segment control() {
			return CONTROL;
		}

		public static Segment wildcard() {
			return WILDCARD;
		}

		public static Segment plain(final String segment) {
			Objects.requireNonNull(segment);
			if (segment.isEmpty() || segment.contains("/")) {
				throw new IllegalArgumentException(String.format("Illegal argument: '%s'", segment));
			}

			return raw(segment);
		}

		public static Segment raw(final String raw) {
			Objects.requireNonNull(raw);

			return new Segment() {

				@Override
				public String render(final Map<String, String> context) {
					return raw;
				}
			};
		}

		public static Segment replace(final String key) {
			return new ReplaceSegment(key);
		}

		public static Segment account() {
			return replace("account-name");
		}

		public static Segment clientId() {
			return replace("client-id");
		}
	}

	private static class ReplaceSegment implements Segment {

		private final String key;

		public ReplaceSegment(final String key) {
			this.key = key;
		}

		@Override
		public String render(final Map<String, String> context) {
			final String value = context.get(this.key);
			if (value == null || value.isEmpty()) {
				throw new IllegalStateException(
						String.format("Unable to replace segment '%s', no value found", this.key));
			}
			return value;
		}

	}

	private final List<Segment> segments;
	private final Map<String, String> context = new HashMap<>();

	private Topic(final List<Segment> segments) {
		this.segments = segments;
	}

	private Topic(final Segment... segments) {
		this.segments = Arrays.asList(segments);
	}

	public String render(final Map<String, String> context) {
		final Map<String, String> ctx = new HashMap<>();
		if (context != null) {
			ctx.putAll(context);
		}
		ctx.putAll(this.context);

		return this.segments.stream().map(seg -> seg.render(ctx)).collect(Collectors.joining("/"));
	}

	public static Topic reply(final String requesterClientId, final String requestId) {
		return new Topic(control(), account(), plain(requesterClientId), replace("application-id"), plain("REPLY"),
				plain(requestId));
	}

	public static Topic application(final String application) {
		return new Topic(control(), account(), clientId(), plain(application));
	}

	public static Topic device(final String localTopic) {
		return new Topic(control(), account(), clientId(), raw(localTopic));
	}

	public Topic append(final Segment segment) {
		final List<Segment> segs = new ArrayList<>(this.segments.size() + 1);
		segs.addAll(this.segments);
		segs.add(segment);
		return new Topic(segs);
	}

	/**
	 * Attach information to the local topic context
	 *
	 * @param key
	 *            the key of the value
	 * @param value
	 *            the value to attach
	 */
	public void attach(final String key, final String value) {
		this.context.put(key, value);
	}
}
