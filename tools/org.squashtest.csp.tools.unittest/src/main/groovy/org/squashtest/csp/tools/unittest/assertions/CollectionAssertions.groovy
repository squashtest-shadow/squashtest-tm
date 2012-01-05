package org.squashtest.csp.tools.unittest.assertions

class CollectionAssertions {
	static def declareContainsExactly() {
		Collection.metaClass.containsExactly { Collection expected ->
			assertContainsExactly(delegate, expected);
			return true;
		}
	}

	private static def assertContainsExactly(def actual, def expected) {
		assert actual.containsAll(expected)
		assert actual.size() == expected.size()
	}

	static def declareContainsExactlyIds() {
		Collection.metaClass.containsExactlyIds { Collection expected ->
			def ids = delegate.collect { it.id }
			assertContainsExactly(ids, expected);
			return true;
		}
	}
}
