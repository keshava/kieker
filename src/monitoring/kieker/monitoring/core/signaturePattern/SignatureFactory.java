/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
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

package kieker.monitoring.core.signaturePattern;

/**
 * @author Bjoern Weissenfels, Jan Waller
 */
public final class SignatureFactory {

	public static final char PATTERN_PREFIX = '%';

	public static final String COLONS = "::";

	/**
	 * Prefix of a cpu signature.
	 */
	public static final String PATTERN_PREFIX_CPU = new StringBuilder(4)
			.append(PATTERN_PREFIX).append("CPU").toString();

	public static final String PATTERN_PREFIX_MEM_SWAP = new StringBuilder(9)
			.append(PATTERN_PREFIX).append("MEM_SWAP").toString();;

	private SignatureFactory() {
		// private default constructor
	}

	/**
	 * Creates a cpu signature with a given cpu id.
	 * 
	 * @param cpuid
	 *            The id of the cpu.
	 * @return
	 *         A signature for the cpu.
	 */
	public static String createCPUSignature(final int cpuid) {
		return new StringBuilder(8)
				.append(PATTERN_PREFIX_CPU)
				.append(COLONS)
				.append(cpuid)
				.toString();
	}

	public static String createCPUSignature() {
		// TODO: Sonderfall beachten
		return PATTERN_PREFIX_CPU;
	}

	public static String createMemSwapSignature() {
		return PATTERN_PREFIX_MEM_SWAP;
	}

	/**
	 * Creates a method signature.
	 * 
	 * @param modList
	 *            List of modifiers in the following order:
	 *            1. public, protected, private, package
	 *            2. abstract, non_abstract
	 *            3. static, non_static
	 *            4. final, non_final
	 *            5. synchronized, non_synchronized
	 *            6. native, non_native
	 *            One or none of each sub-point is allowed.
	 *            Null or empty list stands for any modifiers.
	 * @param retType
	 *            Primitive type ,fully qualified class name or pattern.
	 * @param fqName
	 *            Fully qualified class name or pattern.
	 * @param method
	 *            Method name or pattern.
	 * @param params
	 *            List of primitive types, fully qualified class names or pattern.
	 *            Null or empty list, if no parameters are required.
	 * @param exceptions
	 *            List of exceptions or pattern.
	 *            Null or empty list, if no exceptions are required.
	 * @return
	 *         A signature which has been generated from the inputs.
	 * @throws InvalidPatternException
	 */
	public static String createMethodSignature(final String[] modList, final String retType,
			final String fqName, final String method, final String[] params, final String[] exceptions) throws InvalidPatternException {
		final StringBuilder signature = new StringBuilder(512);
		if (modList != null) {
			for (final String element : modList) {
				signature.append(element);
				signature.append(' ');
			}
		}
		if (retType != null) {
			signature.append(retType);
			signature.append(' ');
		} else {
			throw new InvalidPatternException("return type is requiered");
		}
		if (fqName != null) {
			signature.append(fqName);
			signature.append('.');
		} else {
			throw new InvalidPatternException("fully qualified name is requiered");
		}
		if (method != null) {
			signature.append(method);
			signature.append('(');
		} else {
			throw new InvalidPatternException("method name is requiered");
		}
		if (params != null) {
			signature.append(params[0]);
			for (int i = 1; i < params.length; i++) {
				signature.append(',');
				signature.append(params[i]);
			}
		}
		signature.append(')');
		if (exceptions != null) {
			signature.append(" throws ");
			signature.append(exceptions[0]);
			for (int i = 1; i < exceptions.length; i++) {
				signature.append(',');
				signature.append(exceptions[i]);
			}
		}
		return signature.toString();
	}
}
