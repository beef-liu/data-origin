package com.beef.dataorigin.generator.junittest;

import static org.junit.Assert.*;

import org.junit.Test;

import com.beef.dataorigin.generator.DataOriginGenerator;

public class DataOriginGeneratorTest {

	@Test
	public void testMain1() {
		DataOriginGenerator.main(null);
	}

	public void testMain2() {
		DataOriginGenerator.main(new String[]{"web", "overwrite"});
	}
	
}
