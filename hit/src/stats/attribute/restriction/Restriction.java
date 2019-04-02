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
package stats.attribute.restriction;

import java.util.ArrayList;
import java.util.List;

import stats.StatsEntry;

public abstract class Restriction {
	protected String title=null;
	
	public abstract boolean contains(StatsEntry s);
	
	public List<StatsEntry> apply(List<StatsEntry> set){
		List<StatsEntry> kept = new ArrayList<StatsEntry>();
		for (StatsEntry s : set){
			if (contains(s)){
				kept.add(s);
			}
		}
		return kept;
	}
	
	public void setTitle(String newTitle){
		title=newTitle;
	}
	public String getTitle(){
		if (title==null)return toString();
		return title;
	}
}
