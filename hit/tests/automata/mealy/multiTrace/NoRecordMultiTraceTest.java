package automata.mealy.multiTrace;

class NoRecordMultiTraceTest {

	static class ContractTest implements MultiTraceTest {
		@Override
		public MultiTrace create() {
			return new NoRecordMultiTrace();
		}
	}

}
