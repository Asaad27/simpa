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
