/********************************************************************************
 * Copyright (c) 2017,2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.transparent;

import java.util.Iterator;

import drivers.EnumeratedDriver;
import drivers.ExhaustiveGeneratorOption;
import drivers.mealy.CompleteMealyDriver;
import drivers.mealy.PartialMealyDriver;
import examples.mealy.EnumeratedMealy;
import options.automataOptions.DriverChoice;

public class EnumeratedMealyDriver extends TransparentMealyDriver
		implements EnumeratedDriver {
	EnumeratedMealy automaton;

	EnumeratedMealyDriver(EnumeratedMealy automaton) {
		super(automaton);
		this.automaton = automaton;
	}

	static public class EnumeratedMealyOption
			extends ExhaustiveGeneratorOption<EnumeratedMealyDriver> {

		public EnumeratedMealyOption(
				DriverChoice<? extends PartialMealyDriver> parent) {
			super(parent, EnumeratedMealyDriver.class);
		}

		@Override
		public Iterator<EnumeratedMealyDriver> iterator() {
			return new Iterator<EnumeratedMealyDriver>() {
				EnumeratedMealy.ProducerThread generator = new EnumeratedMealy.ProducerThread();

				@Override
				public boolean hasNext() {
					return generator.hasNext();
				}

				@Override
				public EnumeratedMealyDriver next() {
					return new EnumeratedMealyDriver(generator.next());
				}

			};
		}
	}

	@Override
	public long getSeed() {
		return automaton.getSeed();
	}

}
