package drivers.efsm;

import drivers.efsm.real.GenericDriver;
import drivers.efsm.real.HighWebDriver;
import drivers.efsm.real.LowWebDriver;
import drivers.efsm.real.WGStoredXSSDriver;
import drivers.efsm.real.WebSamlSSOSPDriver;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class EFSMDriverChoice extends DriverChoice<EFSMDriver> {

	public EFSMDriverChoice() {
		super(EFSMDriver.class);
		addChoice(new DriverChoiceItem<EFSMDriver>(this, ArticleDriver.class));
		addChoice(
				new DriverChoiceItem<EFSMDriver>(this, ArticleErrDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, CacheDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this,
				LibcAllocatorDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, NSPK2NDVDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, NSPK2PDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, NSPK3SDriver.class));
		addChoice(new DriverChoiceItem<EFSMDriver>(this, NSPK2NDVDriver.class));
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

}
