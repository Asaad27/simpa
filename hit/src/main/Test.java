package main;

import java.util.Properties;

import org.cafesip.sipunit.Credential;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;


public class Test {
	
	public static void main(String[] args) throws Exception{
		Properties properties1 = new Properties();
		properties1.setProperty("javax.sip.RETRANSMISSION_FILTER", "OFF");	
		SipStack stack = new SipStack(SipStack.DEFAULT_PROTOCOL, SipStack.DEFAULT_PORT, properties1);
		SipPhone phone = stack.createSipPhone("iptel.org", SipStack.PROTOCOL_UDP, 5060, "sip:user1test@iptel.org");
		phone.addUpdateCredential(new Credential("iptel.org", "user1test", "user1test"));
		phone.register(null, 10000);
		SipCall c = phone.createSipCall();
		c.initiateOutgoingCall("sip:user2test@iptel.org", null);
		c.waitForAuthorisation(6000);
		System.out.println(c.getLastReceivedResponse().getStatusCode());
	}
}
