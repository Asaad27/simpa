package drivers.mealy.real;

import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.message.SIPResponse;

import java.util.ArrayList;
import java.util.List;

import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTransaction;

import tools.DigestClientAuthenticationMethod;
import tools.UDPSend;
import tools.Utils;
import tools.loggers.LogManager;

public class SIPDriver extends RealDriver{

	private SipStack stack = null;
	private SipPhone phone = null;
	private String HOST = "82.233.118.237";
	private int PORT = 5070;
	private long cseq = 1;
	private ProxyAuthenticate lastAuthenticate = null;
	
	public SIPDriver() {
		super("SIP");
		try {
			stack = new SipStack(SipStack.DEFAULT_PROTOCOL, SipStack.DEFAULT_PORT);
		} catch (Exception e) {
			LogManager.logException("Error initializing SIP driver", e);
		}
	}
	
	private Response abstractToConcrete(String input){
		Request req = null;
		MessageFactory msg_factory = stack.getMessageFactory();
		AddressFactory addr_factory = stack.getAddressFactory();
		HeaderFactory hdr_factory = stack.getHeaderFactory();
		
		try {
			if (input.equals("REGISTER")){
				URI uri = addr_factory.createURI("sip:iptel.org");
				Address to_address = addr_factory.createAddress(addr_factory.createURI("sip:user1test@iptel.org"));					
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add(hdr_factory.createViaHeader(HOST, PORT, "udp", null));
				Address contact_address = addr_factory.createAddress("sip:user1test@"+ HOST + ":" +PORT);

				req = msg_factory.createRequest(
						uri,
						Request.REGISTER,
						hdr_factory.createCallIdHeader("KARIM1@192.168.1.1"),
						hdr_factory.createCSeqHeader(cseq++, Request.REGISTER),
						hdr_factory.createFromHeader(phone.getAddress(), phone.generateNewTag()),
						hdr_factory.createToHeader(to_address, null),
						viaHeaders,
						hdr_factory.createMaxForwardsHeader(70));					
				req.addHeader(hdr_factory.createContactHeader(contact_address));
				ArrayList<String> userAgents = new ArrayList<String>();
				userAgents.add("SIMPA/SIPClient");
				req.addHeader(hdr_factory.createUserAgentHeader(userAgents));

				Response resp = msg_factory.createResponse(UDPSend.Send("iptel.org", 5060, req.toString()));
				
				if (resp.getStatusCode() == 401){
					lastAuthenticate = (ProxyAuthenticate) resp.getHeader("Proxy-Authenticate");

					req = msg_factory.createRequest(
							uri,
							Request.REGISTER,
							hdr_factory.createCallIdHeader("KARIM1@192.168.1.1"),
							hdr_factory.createCSeqHeader(cseq++, Request.REGISTER),
							hdr_factory.createFromHeader(phone.getAddress(), phone.generateNewTag()),
							hdr_factory.createToHeader(to_address, null),
							viaHeaders,
							hdr_factory.createMaxForwardsHeader(70));					
					req.addHeader(hdr_factory.createContactHeader(contact_address));
					req.addHeader(hdr_factory.createUserAgentHeader(userAgents));

					AuthorizationHeader auth_hdr = hdr_factory.createAuthorizationHeader("Digest");
					auth_hdr.setAlgorithm("MD5");
					auth_hdr.setNonce(lastAuthenticate.getNonce());
					auth_hdr.setRealm(lastAuthenticate.getRealm());
					auth_hdr.setUsername("user1test");
					auth_hdr.setURI(uri);

					DigestClientAuthenticationMethod m = new DigestClientAuthenticationMethod();
					m.initialize(auth_hdr.getRealm(), auth_hdr.getUsername(), auth_hdr.getURI().toString(), auth_hdr.getNonce(), auth_hdr.getUsername(), Request.REGISTER, auth_hdr.getCNonce(), auth_hdr.getAlgorithm());
					auth_hdr.setResponse(m.generateResponse());						
					req.addHeader(auth_hdr);

					resp = msg_factory.createResponse(UDPSend.Send("iptel.org", 5060, req.toString()));	            
				}else
					lastAuthenticate = null;
				return resp;		            
			}else if (input.equals("INVITE")){
				URI to = addr_factory.createURI("sip:user2test@iptel.org");
				Address to_address = addr_factory.createAddress(to);				
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				viaHeaders.add((ViaHeader) phone.getViaHeaders().get(0));				
				viaHeaders.add(hdr_factory.createViaHeader(HOST, PORT, "udp", null));
				Address contact_address = addr_factory.createAddress("sip:user1test@"+ HOST + ":" +PORT);

				req = msg_factory.createRequest(
						to,
						Request.INVITE,
						hdr_factory.createCallIdHeader("KARIM1@192.168.1.1"),
						hdr_factory.createCSeqHeader(cseq++, Request.INVITE),
						hdr_factory.createFromHeader(phone.getAddress(), phone.generateNewTag()),
						hdr_factory.createToHeader(to_address, null),
						viaHeaders,
						hdr_factory.createMaxForwardsHeader(70));					
				req.addHeader(hdr_factory.createContactHeader(contact_address));
				ArrayList<String> userAgents = new ArrayList<String>();
				userAgents.add("SIMPA/SIPClient");
				req.addHeader(hdr_factory.createUserAgentHeader(userAgents));

				Response resp = msg_factory.createResponse(UDPSend.Send("iptel.org", 5060, req.toString()));	     

				if (resp.getStatusCode() == 407){
					lastAuthenticate = (ProxyAuthenticate) resp.getHeader("Proxy-Authenticate");

					req = msg_factory.createRequest(
							to,
							Request.INVITE,
							hdr_factory.createCallIdHeader("KARIM1@192.168.1.1"),
							hdr_factory.createCSeqHeader(cseq++, Request.INVITE),
							hdr_factory.createFromHeader(phone.getAddress(), phone.generateNewTag()),
							hdr_factory.createToHeader(to_address, null),
							viaHeaders,
							hdr_factory.createMaxForwardsHeader(70));					
					req.addHeader(hdr_factory.createContactHeader(contact_address));
					req.addHeader(hdr_factory.createUserAgentHeader(userAgents));

					AuthorizationHeader auth_hdr = hdr_factory.createAuthorizationHeader("Digest");
					auth_hdr.setAlgorithm("MD5");
					auth_hdr.setNonce(lastAuthenticate.getNonce());
					auth_hdr.setRealm(lastAuthenticate.getRealm());
					auth_hdr.setUsername("user1test");
					auth_hdr.setURI(to);

					DigestClientAuthenticationMethod m = new DigestClientAuthenticationMethod();
					m.initialize(auth_hdr.getRealm(), auth_hdr.getUsername(), auth_hdr.getURI().toString(), auth_hdr.getNonce(), auth_hdr.getUsername(), Request.INVITE, auth_hdr.getCNonce(), auth_hdr.getAlgorithm());
					auth_hdr.setResponse(m.generateResponse());						
					req.addHeader(auth_hdr);

					resp = msg_factory.createResponse(UDPSend.Send("iptel.org", 5060, req.toString()));	            
				}else
					lastAuthenticate = null;
				return resp;
			}
		} catch (Exception e) {
			LogManager.logException("Error concretizing " + input + " request", e);
		}
		return null;
	}
	
	private String concreteToAbstract(Response resp){
		return String.valueOf(resp.getStatusCode());		
	}

	@Override
	public String execute(String input) {
		String output = concreteToAbstract(abstractToConcrete(input));
		LogManager.logRequest(input, output);
		return output;
	}

	@Override
	public void reset() {
		try {
			cseq = 1;
			if (phone != null) {
				phone.dispose();
			}
			phone = stack.createSipPhone("iptel.org", SipStack.PROTOCOL_UDP, 5060, "sip:user1test@iptel.org");
		} catch (Exception e) {
			LogManager.logException("Error reseting SIP driver", e);
		}
	}

	@Override
	public List<String> getInputSymbols() {
		return Utils.createArrayList("INVITE", "ACK");
	}

	@Override
	public List<String> getOutputSymbols() {
		return Utils.createArrayList("200", "401");
	}

}
