/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.analysisteetime.signature;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import kieker.analysisteetime.model.analysismodel.type.ComponentType;
import kieker.analysisteetime.model.analysismodel.type.TypeFactory;
import kieker.analysisteetime.signature.JavaComponentSignatureExtractor;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public class JavaComponentSignatureExtractorTest {

	private JavaComponentSignatureExtractor signatureExtractor;

	@Before
	public void setUp() throws Exception {
		this.signatureExtractor = new JavaComponentSignatureExtractor();
	}

	@After
	public void tearDown() throws Exception {
		this.signatureExtractor = null;
	}

	/**
	 * Test method for {@link kieker.analysisteetime.signature.JavaComponentSignatureExtractor#extract(kieker.analysisteetime.model.analysismodel.type.ComponentType)}.
	 */
	@Test
	public void testExtractWithNonNestedPackage() {
		final String packageName = "package";
		final String componentName = "MyClass";

		this.testExtraction(packageName, componentName);
	}

	/**
	 * Test method for {@link kieker.analysisteetime.signature.JavaComponentSignatureExtractor#extract(kieker.analysisteetime.model.analysismodel.type.ComponentType)}.
	 */
	@Test
	public void testExtractWithNestedPackage() {
		final String packageName = "com.company.package";
		final String componentName = "MyClass";

		this.testExtraction(packageName, componentName);
	}

	/**
	 * Test method for {@link kieker.analysisteetime.signature.JavaComponentSignatureExtractor#extract(kieker.analysisteetime.model.analysismodel.type.ComponentType)}.
	 */
	@Test
	public void testExtractWithoutPackage() {
		final String packageName = "";
		final String componentName = "MyClass";

		this.testExtraction(packageName, componentName);
	}

	/**
	 * Test method for {@link kieker.analysisteetime.signature.JavaComponentSignatureExtractor#extract(kieker.analysisteetime.model.analysismodel.type.ComponentType)}.
	 */
	@Test
	public void testExtractWithInnerClass() {
		final String packageName = "com.company.package";
		final String componentName = "MyClass$InnerClass";

		this.testExtraction(packageName, componentName);
	}

	private void testExtraction(final String packageName, final String name) {
		final ComponentType componentType = this.buildComponentTypeByNames(packageName, name);

		this.signatureExtractor.extract(componentType);

		final String extractedPackage = componentType.getPackage();
		final String extractedName = componentType.getName();

		Assert.assertEquals(packageName, extractedPackage);
		Assert.assertEquals(name, extractedName);
	}

	private ComponentType buildComponentTypeBySiganture(final String signature) {
		final ComponentType componentType = TypeFactory.eINSTANCE.createComponentType();
		componentType.setSignature(signature);
		return componentType;
	}

	private ComponentType buildComponentTypeByNames(final String packageName, final String name) {
		return this.buildComponentTypeBySiganture(packageName + '.' + name);
	}

}