/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package learner.mealy.localizerBased.dataManager;

import learner.mealy.LmTrace;

public class FullyKnownTrace {
	private FullyQualifiedState start;
	private FullyQualifiedState end;
	private LmTrace trace;
	
	public FullyKnownTrace(FullyQualifiedState start, LmTrace trace, FullyQualifiedState end){
		this.start = start;
		this.trace = trace;
		this.end = end;
	}
	
	public FullyQualifiedState getStart(){
		return start;
	}
	
	public FullyQualifiedState getEnd(){
		return end;
	}
	
	public LmTrace getTrace(){
		return trace;//a secure way is to return trace.clone(). Should we use it ?
	}
	
	public String toString(){
		return start.toString() + 
				" followed by " + trace.toString() +
				" → " + end.toString();
	}
}
