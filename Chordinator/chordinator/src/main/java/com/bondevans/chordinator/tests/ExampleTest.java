package com.bondevans.chordinator.tests;

import android.test.InstrumentationTestCase;

/**
 * Created by Paul on 07/03/2015.
 */
public class ExampleTest extends InstrumentationTestCase {
	public void test() throws Exception {
		final int expected = 5;
		final int reality = 5;
		assertEquals(expected, reality);
	}
}
