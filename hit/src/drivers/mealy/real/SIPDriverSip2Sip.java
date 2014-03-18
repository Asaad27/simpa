package drivers.mealy.real;

import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.WWWAuthenticate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;

import tools.DigestClientAuthenticationMethod;
import tools.UDPSend;
import tools.Utils;
import tools.loggers.LogManager;

public class SIPDriverSip2Sip extends RealDriver {

	private SipStack stack = null;
	private SipPhone phone = null;
	private MessageFactory msg_factory;
	private AddressFactory addr_factory;
	private HeaderFactory hdr_factory;

	private String HOST = "82.233.118.237";
	private int PORT = 5070;
	private long cseq = 1;
	private Response lastResp = null;
	private AuthorizationHeader lastInvite = null;
	private RecordRouteHeader lastRoute = null;

	public SIPDriverSip2Sip() {
		super("SIP");
		try {
			stack = new SipStack(SipStack.DEFAULT_PROTOCOL,
					SipStack.DEFAULT_PORT);
			msg_factory = stack.getMessageFactory();
			addr_factory = stack.getAddressFactory();
			hdr_factory = stack.getHeaderFactory();
			outputSymbols = new ArrayList<String>();
		} catch (Exception e) {
			LogManager.logException("Error initializing SIP driver", e);
		}
	}

	private Request abstractToConcrete(String input) {
		Request req = null;

		try {
			if (input.equals("REGISTER")) {
				cseq++;
				URI uri = addr_factory.createURI("sip:sip2sip.info");
				Address to_address = addr_factory.createAddress(addr_factory
						.createURI("sip:user3test@sip2sip.info"));
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				ViaHeader via = (ViaHeader) phone.getViaHeaders().get(0);
				via.setBranch("z9hG4bK3" + cseq + "2632");
				viaHeaders.add(via);

				req = msg_factory.createRequest(
						uri,
						Request.REGISTER,
						hdr_factory.createCallIdHeader("STARGATE@" + HOST),
						hdr_factory.createCSeqHeader(cseq, Request.REGISTER),
						hdr_factory.createFromHeader(phone.getAddress(),
								phone.generateNewTag()),
						hdr_factory.createToHeader(to_address, null),
						viaHeaders, hdr_factory.createMaxForwardsHeader(70));

				Address contact_address = addr_factory
						.createAddress("sip:user3test@" + HOST + ":" + PORT);
				req.addHeader(hdr_factory.createContactHeader(contact_address));
				ArrayList<String> userAgents = new ArrayList<String>();
				userAgents.add("SIMPA/SIPClient");
				req.addHeader(hdr_factory.createUserAgentHeader(userAgents));

				String respStr = UDPSend.Send("sip2sip.info", 5060, req);
				Response resp = msg_factory.createResponse(respStr);

				if (resp != null && resp.getHeader("WWW-Authenticate") != null) {
					cseq++;

					viaHeaders = new ArrayList<ViaHeader>();
					via = (ViaHeader) phone.getViaHeaders().get(0);
					via.setBranch("z9hG4bK3" + cseq + "2632");
					viaHeaders.add(via);

					req = msg_factory
							.createRequest(uri, Request.REGISTER, hdr_factory
									.createCallIdHeader("STARGATE@" + HOST),
									hdr_factory.createCSeqHeader(cseq,
											Request.REGISTER), hdr_factory
											.createFromHeader(
													phone.getAddress(),
													phone.generateNewTag()),
									hdr_factory
											.createToHeader(to_address, null),
									viaHeaders, hdr_factory
											.createMaxForwardsHeader(70));

					req.addHeader(hdr_factory
							.createContactHeader(contact_address));
					req.addHeader(hdr_factory.createUserAgentHeader(userAgents));

					WWWAuthenticate auth = (WWWAuthenticate) resp
							.getHeader("WWW-Authenticate");
					AuthorizationHeader auth_hdr = hdr_factory
							.createAuthorizationHeader("Digest");
					auth_hdr.setAlgorithm("MD5");
					auth_hdr.setNonce(auth.getNonce());
					auth_hdr.setRealm(auth.getRealm());
					auth_hdr.setUsername("user3test");
					auth_hdr.setURI(uri);

					DigestClientAuthenticationMethod m = new DigestClientAuthenticationMethod();
					m.initialize(auth_hdr.getRealm(), auth_hdr.getUsername(),
							auth_hdr.getURI().toString(), auth_hdr.getNonce(),
							auth_hdr.getUsername(), Request.REGISTER,
							auth_hdr.getCNonce(), auth_hdr.getAlgorithm());
					auth_hdr.setResponse(m.generateResponse());
					req.addHeader(auth_hdr);
				}

			} else if (input.equals("INVITE")) {
				cseq++;
				lastInvite = null;
				lastRoute = null;
				URI to = addr_factory.createURI("sip:4444@proxy.sipthor.net");
				Address to_address = addr_factory.createAddress(to);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				ViaHeader via = (ViaHeader) phone.getViaHeaders().get(0);
				via.setBranch("z9hG4bK3" + cseq + "2632");
				viaHeaders.add(via);
				Address contact_address = addr_factory
						.createAddress("sip:user3test@" + HOST + ":" + PORT);

				req = msg_factory.createRequest(
						to,
						Request.INVITE,
						hdr_factory.createCallIdHeader("STARGATE@" + HOST),
						hdr_factory.createCSeqHeader(cseq, Request.INVITE),
						(lastResp == null ? hdr_factory.createFromHeader(
								phone.getAddress(), phone.generateNewTag())
								: (FromHeader) lastResp.getHeader("From")),
						hdr_factory.createToHeader(to_address, null),
						viaHeaders, hdr_factory.createMaxForwardsHeader(70));
				req.addHeader(hdr_factory.createContactHeader(contact_address));
				ArrayList<String> userAgents = new ArrayList<String>();
				userAgents.add("SIMPA/SIPClient");
				req.addHeader(hdr_factory.createUserAgentHeader(userAgents));
				req.addHeader(hdr_factory
						.createAllowHeader("INVITE, ACK, CANCEL, OPTIONS, BYE, INFO, REFER, NOTIFY"));
				req.addHeader(hdr_factory.createSupportedHeader("replaces"));

				req.setContent(
						"v=0\no=SIMPA-Talk 1352071263 1352071272 IN IP4 82.233.118.237\ns=SIMPA Call\nc=IN IP4 82.233.118.237\nt=0 0\nm=audio 8000 RTP/AVP 0 8 96 3 13 101\na=rtpmap:0 PCMU/8000\na=rtpmap:8 PCMA/8000\na=rtpmap:96 G726-32/8000\na=rtpmap:3 GSM/8000\na=rtpmap:13 CN/8000\na=rtpmap:101 telephone-event/8000\na=fmtp:101 0-16\na=sendrecv\na=local:192.168.1.1 8000\na=domain:82.233.118.237\n",
						hdr_factory.createContentTypeHeader("application",
								"sdp"));

				if (lastResp != null
						&& lastResp.getHeader("Proxy-Authenticate") != null) {
					ProxyAuthenticate auth = (ProxyAuthenticate) lastResp
							.getHeader("Proxy-Authenticate");
					ProxyAuthorizationHeader auth_hdr = hdr_factory
							.createProxyAuthorizationHeader("Digest");
					auth_hdr.setAlgorithm("MD5");
					auth_hdr.setNonce(auth.getNonce());
					auth_hdr.setRealm(auth.getRealm());
					auth_hdr.setUsername("user3test");
					auth_hdr.setURI(to);

					DigestClientAuthenticationMethod m = new DigestClientAuthenticationMethod();
					m.initialize(auth_hdr.getRealm(), auth_hdr.getUsername(),
							auth_hdr.getURI().toString(), auth_hdr.getNonce(),
							auth_hdr.getUsername(), Request.INVITE,
							auth_hdr.getCNonce(), auth_hdr.getAlgorithm());
					auth_hdr.setResponse(m.generateResponse());
					req.addHeader(auth_hdr);
					lastInvite = auth_hdr;
				}

			} else if (input.equals("ACK")) {
				URI to = addr_factory.createURI("4444@proxy.sipthor.net");
				Address to_address = addr_factory.createAddress(to);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				ViaHeader via = (ViaHeader) phone.getViaHeaders().get(0);
				via.setBranch("z9hG4bK3" + cseq + "2632");
				viaHeaders.add(via);

				req = msg_factory.createRequest(
						to,
						Request.ACK,
						hdr_factory.createCallIdHeader("STARGATE@" + HOST),
						hdr_factory.createCSeqHeader(cseq, Request.ACK),
						(lastResp == null ? hdr_factory.createFromHeader(
								phone.getAddress(), phone.generateNewTag())
								: (FromHeader) lastResp.getHeader("From")),
						(lastResp == null ? hdr_factory.createToHeader(
								to_address, null) : (ToHeader) lastResp
								.getHeader("To")), viaHeaders, hdr_factory
								.createMaxForwardsHeader(70));

				if (lastInvite != null) {
					req.addHeader(lastInvite);
				}
			} else if (input.equals("BYE")) {
				cseq++;
				URI to = addr_factory.createURI("4444@proxy.sipthor.net");
				Address to_address = addr_factory.createAddress(to);
				ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
				ViaHeader via = (ViaHeader) phone.getViaHeaders().get(0);
				via.setBranch("z9hG4bK3" + cseq + "2632");
				viaHeaders.add(via);

				req = msg_factory.createRequest(
						to,
						Request.BYE,
						hdr_factory.createCallIdHeader("STARGATE@" + HOST),
						hdr_factory.createCSeqHeader(cseq, Request.BYE),
						(lastResp == null ? hdr_factory.createFromHeader(
								phone.getAddress(), phone.generateNewTag())
								: (FromHeader) lastResp.getHeader("From")),
						(lastResp == null ? hdr_factory.createToHeader(
								to_address, null) : (ToHeader) lastResp
								.getHeader("To")), viaHeaders, hdr_factory
								.createMaxForwardsHeader(70));

				if (lastRoute != null) {
					req.addHeader(hdr_factory.createRouteHeader(lastRoute
							.getAddress()));
				}

				String respStr = UDPSend.Send("sip2sip.info", 5060, req);
				Response resp = msg_factory.createResponse(respStr);

				if (resp != null
						&& resp.getHeader("Proxy-Authenticate") != null) {
					cseq++;

					viaHeaders = new ArrayList<ViaHeader>();
					via = (ViaHeader) phone.getViaHeaders().get(0);
					via.setBranch("z9hG4bK3" + cseq + "2632");
					viaHeaders.add(via);

					req = msg_factory.createRequest(
							to,
							Request.BYE,
							hdr_factory.createCallIdHeader("STARGATE@" + HOST),
							hdr_factory.createCSeqHeader(cseq, Request.BYE),
							(lastResp == null ? hdr_factory.createFromHeader(
									phone.getAddress(), phone.generateNewTag())
									: (FromHeader) lastResp.getHeader("From")),
							(lastResp == null ? hdr_factory.createToHeader(
									to_address, null) : (ToHeader) lastResp
									.getHeader("To")), viaHeaders, hdr_factory
									.createMaxForwardsHeader(70));

					ProxyAuthenticate auth = (ProxyAuthenticate) resp
							.getHeader("Proxy-Authenticate");
					AuthorizationHeader auth_hdr = hdr_factory
							.createAuthorizationHeader("Digest");
					auth_hdr.setAlgorithm("MD5");
					auth_hdr.setNonce(auth.getNonce());
					auth_hdr.setRealm(auth.getRealm());
					auth_hdr.setUsername("user3test");
					auth_hdr.setURI(to);

					DigestClientAuthenticationMethod m = new DigestClientAuthenticationMethod();
					m.initialize(auth_hdr.getRealm(), auth_hdr.getUsername(),
							auth_hdr.getURI().toString(), auth_hdr.getNonce(),
							auth_hdr.getUsername(), Request.REGISTER,
							auth_hdr.getCNonce(), auth_hdr.getAlgorithm());
					auth_hdr.setResponse(m.generateResponse());
					req.addHeader(auth_hdr);
				}
			}
		} catch (Exception e) {
			LogManager.logException("Error concretizing " + input + " request",
					e);
		}
		return req;
	}

	private String concreteToAbstract(Response resp) {
		return String.valueOf(resp.getStatusCode());
	}

	@Override
	public String execute(String input) {
		numberOfAtomicRequest++;
		Request req = abstractToConcrete(input);
		LogManager.logInfo("\n" + req.toString());
		Response resp = null;
		String output = null;
		try {
			String respStr = UDPSend.Send("sip2sip.info", 5060, req);
			LogManager.logInfo("\n" + respStr);
			if (!respStr.equals("Timeout")) {
				resp = msg_factory.createResponse(respStr);
				if (resp.getHeader("Record-Route") != null)
					lastRoute = (RecordRouteHeader) resp
							.getHeader("Record-Route");
				lastResp = resp;
				output = concreteToAbstract(resp);
			} else {
				output = "TIMEOUT";
			}
		} catch (ParseException e) {
			LogManager.logException("Unable to parse response", e);
		}
		if (!outputSymbols.contains(output))
			outputSymbols.add(output);
		LogManager.logRequest(input, output);
		return output;
	}

	@Override
	public void reset() {
		super.reset();
		try {
			cseq += 5;
			if (phone != null) {
				phone.dispose();
			}
			phone = stack.createSipPhone("sip2sip.info", SipStack.PROTOCOL_UDP,
					5060, "sip:user3test@sip2sip.info");
			lastResp = null;
		} catch (Exception e) {
			LogManager.logException("Error reseting SIP driver", e);
		}
	}

	@Override
	public List<String> getInputSymbols() {
		return Utils.createArrayList("REGISTER", "INVITE", "ACK", "BYE");
	}
}
