package com.beef.dataorigin.generator.junittest;

import static org.junit.Assert.*;

import org.junit.Test;

import com.beef.dataorigin.generator.DataOriginGenerator;

public class DataOriginGeneratorTest {

	public void testMain1() {
		DataOriginGenerator.main(null);
	}

	@Test
	public void testMain2() {
		DataOriginGenerator.main(new String[]{"web"});
	}

	public void testMain3() {
		DataOriginGenerator.main(new String[]{"web", "overwrite"});
	}
	
}
