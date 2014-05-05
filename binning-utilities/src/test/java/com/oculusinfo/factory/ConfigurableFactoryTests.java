/*
 * Copyright (c) 2014 Oculus Info Inc. http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.factory;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.oculusinfo.factory.properties.DoubleProperty;
import com.oculusinfo.factory.properties.IntegerProperty;
import com.oculusinfo.factory.properties.StringProperty;

public class ConfigurableFactoryTests {
	private static IntegerProperty INT_PROP = new IntegerProperty("int", "", 1);
	private static DoubleProperty DOUBLE_PROP = new DoubleProperty("double", "", 1.0);
	private static StringProperty STRING_PROP = new StringProperty("string", "", "1.");
	private class FactoryResult {
		int _int;
		double _double;
		String _string;
	}
	private class TestFactory extends ConfigurableFactory<FactoryResult> {
		public TestFactory () {
			super(FactoryResult.class, null, null);
			addProperty(INT_PROP);
			addProperty(DOUBLE_PROP);
			addProperty(STRING_PROP);
		}
		@Override
		protected FactoryResult create () {
			FactoryResult result = new FactoryResult();
			result._int = getPropertyValue(INT_PROP);
			result._double = getPropertyValue(DOUBLE_PROP);
			result._string = getPropertyValue(STRING_PROP);
			return result;
		}
	}

	@Test
	public void testDefaultConfiguration () throws Exception {
		TestFactory factory = new TestFactory();
		JSONObject configuration = new JSONObject("{}");
		factory.readConfiguration(configuration);
		FactoryResult result = factory.produce(FactoryResult.class);
		Assert.assertEquals(1, result._int);
		Assert.assertEquals(1.0, result._double, 1E-12);
		Assert.assertEquals("1.", result._string);
	}

	@Test
	public void testReadConfiguration () throws Exception {
		TestFactory factory = new TestFactory();
		JSONObject configuration = new JSONObject("{'int':3, 'double': 4.2, 'string': 'abc'}");
		factory.readConfiguration(configuration);
		FactoryResult result = factory.produce(FactoryResult.class);
		Assert.assertEquals(3, result._int);
		Assert.assertEquals(4.2, result._double, 1E-12);
		Assert.assertEquals("abc", result._string);
	}
}
