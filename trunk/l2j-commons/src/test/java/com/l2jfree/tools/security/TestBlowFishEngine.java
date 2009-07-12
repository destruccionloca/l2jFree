package com.l2jfree.tools.security;

import java.io.IOException;

import junit.framework.TestCase;

public class TestBlowFishEngine extends TestCase {
	public void testAlgorithmName() {
		BlowfishEngine bf = new BlowfishEngine();
		assertEquals("Blowfish", bf.getAlgorithmName());
	}

	public void testBlockSize() {
		BlowfishEngine bf = new BlowfishEngine();
		assertEquals(8, bf.getBlockSize());
	}

	public void testProcessBlockWithTooSmallInputBuffer() {
		BlowfishEngine bf = new BlowfishEngine();
		byte[] byArray = new byte[] { (byte) 0x12, (byte) 0x0F, (byte) 0xF0 };
		byte[] result = new byte[byArray.length];
		try {
			bf.init(true, byArray);
			bf.processBlock(byArray, 0, result, 0);
			fail("input buffer should be too small");
		} catch (IOException e) {
			assertNotNull(e);
			assertTrue(e.getMessage().startsWith("input buffer"));
		}
	}

	public void testProcessBlockWithTooSmallOutputBuffer() {
		BlowfishEngine bf = new BlowfishEngine();
		byte[] byArray = new byte[] { (byte) 0x12, (byte) 0x0F, (byte) 0xF0,
				(byte) 0xF1, (byte) 0x13, (byte) 0x1F, (byte) 0x12, (byte) 0x00 };
		byte[] result = new byte[byArray.length];
		try {
			bf.init(true, byArray);
			bf.processBlock(byArray, 0, result, 3);
			fail("output buffer should be too small");
		} catch (IOException e) {
			assertNotNull(e);
			assertTrue(e.getMessage().startsWith("output buffer"));
		}
	}

	public void testProcessBlockEncrypt() {
		BlowfishEngine bf = new BlowfishEngine();
		byte[] byArray = new byte[] { (byte) 0x12, (byte) 0x0F, (byte) 0xF0,
				(byte) 0xF1, (byte) 0x13, (byte) 0x1F, (byte) 0x12, (byte) 0x00 };
		byte[] result = new byte[byArray.length];
		try {
			bf.init(true, byArray);
			bf.processBlock(byArray, 0, result, 0);
		} catch (IOException e) {
			fail("decryption should work");
		}
	}

	public void testProcessBlockDecrypt() {
		BlowfishEngine bf = new BlowfishEngine();
		byte[] byArray = new byte[] { (byte) 0x12, (byte) 0x0F, (byte) 0xF0,
				(byte) 0xF1, (byte) 0x13, (byte) 0x1F, (byte) 0x12, (byte) 0x00 };
		byte[] result = new byte[byArray.length];
		try {
			bf.init(false, byArray);
			bf.processBlock(byArray, 0, result, 0);
		} catch (IOException e) {
			fail("decryption should work");
		}
	}

	public void testProcessBlockNotInitialized() {
		BlowfishEngine bf = new BlowfishEngine();
		byte[] byArray = new byte[] { (byte) 0x12, (byte) 0x0F, (byte) 0xF0,
				(byte) 0xF1, (byte) 0x13, (byte) 0x1F, (byte) 0x12, (byte) 0x00 };
		byte[] result = new byte[byArray.length];
		try {
			bf.processBlock(byArray, 0, result, 0);
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
