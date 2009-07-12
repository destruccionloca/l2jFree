package com.l2jfree.tools.math;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

public class TestScrambledKeyPair extends TestCase {

	public void testSCrambleKeyPair() {
		KeyPairGenerator _keyGen = null;
		try {
			_keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			fail(e.getMessage());
		}
		KeyPair kp = _keyGen.generateKeyPair();
		ScrambledKeyPair skp1 = new ScrambledKeyPair(kp);
		ScrambledKeyPair skp2 = new ScrambledKeyPair(kp);
		byte[] bySkp1 = skp1.getScrambledModulus();
		byte[] bySkp2 = skp2.getScrambledModulus();

		assertEquals(bySkp1.length, bySkp2.length);
		// check that two byte array generated with the same key are the same
		for (int i = 0; i < bySkp1.length; i++) {
			assertEquals(bySkp1[i], bySkp2[i]);
		}

	}
}
