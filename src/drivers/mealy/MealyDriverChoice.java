/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy;

import java.util.List;

import drivers.ExhaustiveGeneratorOption;
import drivers.mealy.real.SIPDriverIPTel;
import drivers.mealy.real.mqtt.MQTTDriverOption;
import drivers.mealy.simulation.ScanetteDriverOption;
import drivers.mealy.transparent.EnumeratedMealyDriver.EnumeratedMealyOption;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentFromDotMealyDriver;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class MealyDriverChoice extends DriverChoice<PartialMealyDriver> {
	DriverChoiceItem<CompleteMealyDriver> SIPDriverIpTel = new DriverChoiceItem<CompleteMealyDriver>(
			this, SIPDriverIPTel.class);
	public final ExhaustiveGeneratorOption<? extends PartialMealyDriver> exhaustiveDriver = new EnumeratedMealyOption(
			this);

	public MealyDriverChoice() {
		super(PartialMealyDriver.class, "Mealy driver");
		addChoice(SIPDriverIpTel);
		addChoice(new DriverChoiceItem<PartialMealyDriver>(this,
				RandomMealyDriver.class));
		// addChoice(new FromDotMealyDriver.FromDotChoiceItem(this));
		addChoice(new TransparentFromDotMealyDriver.FromDotChoiceItem(this));
		addChoice(exhaustiveDriver);
		addChoice(new MQTTDriverOption(this));
		addChoice(new ScanetteDriverOption(this));
	}

	@Override
	public List<SampleArgumentValue> getSampleArgumentValues(
			ArgumentDescriptor arg) {
		List<SampleArgumentValue> result = super.getSampleArgumentValues(arg);
		for (SampleArgumentValue sample : result) {
			if (sample.value.equals(exhaustiveDriver.argValue))
				sample.hide();
		}
		return result;
	}
}
