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
package stats.table;

public class TableCell {
	private boolean isHeader;
	private String formatedContent;

	public String getFormatedContent() {
		return formatedContent;
	}

	public void setFormatedContent(String formatedContent) {
		this.formatedContent = formatedContent;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public boolean isHeader() {
		return isHeader;
	}

}
