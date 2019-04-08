/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package tools;

import java.io.OutputStream;
import java.io.PrintStream;

public class NullStream extends PrintStream {

	public NullStream() {
		super(new OutputStream() {

			@Override
			public void write(int b) {
			}
		});
	}
}
