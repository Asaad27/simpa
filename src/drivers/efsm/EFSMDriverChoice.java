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
package drivers.efsm;

import java.util.ArrayList;
import java.util.List;

import drivers.efsm.real.GenericDriver;
import drivers.efsm.real.HighWebDriver;
import drivers.efsm.real.LowWebDriver;
import drivers.efsm.real.WGStoredXSSDriver;
import drivers.efsm.real.WebSamlSSOSPDriver;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class EFSMDriverChoice extends DriverChoice<EFSMDriver> {
	List<DriverChoiceItem<EFSMDriver>> hiddenChoices = new ArrayList<>();

	public EFSMDriverChoice() {
		super(EFSMDriver.class, "EFSM driver");
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, ArticleDriver.class));
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, ArticleErrDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, CacheDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this,
				LibcAllocatorDriver.class));
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, NSPK2NDVDriver.class));
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, NSPK2PDriver.class));
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, NSPKDriver.class));
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, NSPK3SDriver.class));
		addHiddenChoice(
				new DriverChoiceItem<EFSMDriver>(this, NSPK2NDVDriver.class));
		addChoice(
				new DriverChoiceItem<EFSMDriver>(this, SamlSSOSPDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, CacheDriver.class));
		addChoice(new RandomEFSMDriver.RandomDriveroption(this));
		addChoice(new GenericDriver.GenericDriveroption(this));
		// addChoice(new ScanDriver.ScanDriveroption(this));
		// FIXME What is exactly SCAN ? Is it EFSM ?
		addChoice(new DriverChoiceItem<EFSMDriver>(this, HighWebDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, LowWebDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this,
				WebSamlSSOSPDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this,
				WGStoredXSSDriver.class));
	}

	private void addHiddenChoice(DriverChoiceItem<EFSMDriver> choice) {
		hiddenChoices.add(choice);
		addChoice(choice);
	}

	@Override
	public List<SampleArgumentValue> getSampleArgumentValues(
			ArgumentDescriptor arg) {
		List<SampleArgumentValue> result = super.getSampleArgumentValues(arg);
		for (SampleArgumentValue sample : result) {
			for (DriverChoiceItem<EFSMDriver> choice : hiddenChoices) {
				if (choice.argValue.equals(sample.value))
					sample.hide();
			}
		}
		return result;
	}

}
