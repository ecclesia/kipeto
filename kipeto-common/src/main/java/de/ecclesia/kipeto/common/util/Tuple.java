/*
 * #%L
 * Kipeto Common
 * %%
 * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.ecclesia.kipeto.common.util;

public class Tuple<A> {

	public static <A> Tuple<A> newTuple(A a) {
		return new Tuple<A>(a);
	}

	public static <A, B> Tuple2<A, B> newTuple(A a, B b) {
		return new Tuple2<A, B>(a, b);
	}

	private A a;

	protected Tuple() {
	}

	protected Tuple(A a) {
		this.a = a;
	}

	public A get1() {
		return a;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!obj.getClass().equals(this.getClass())) {
			return false;
		}

		return obj.toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	protected final String joinValues(String existingString, Object value) {
		return existingString + ", " + valueToString(value);
	}

	protected String valueToString() {
		return valueToString(a);
	}

	private String valueToString(Object value) {
		return String.valueOf(value) + ((value != null) ? ":" + value.getClass().getName() : "");
	}

	public final String toString() {
		return getClass().getSimpleName() + "[" + valueToString() + "]";
	}
	
	public static class Tuple2<A, B> extends Tuple<A> {

		protected B b;

		protected Tuple2() {
			super();
		}

		protected Tuple2(A a, B b) {
			super(a);
			this.b = b;
		}

		public B get2() {
			return b;
		}

		@Override
		protected String valueToString() {
			return joinValues(super.valueToString(), b);
		}

	}

}
