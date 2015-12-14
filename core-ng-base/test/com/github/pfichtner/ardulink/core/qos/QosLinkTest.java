package com.github.pfichtner.ardulink.core.qos;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol255;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class QosLinkTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Rule
	public Arduino arduino = Arduino.newArduino();

	private ConnectionBasedQosLink qosLink;

	@After
	public void tearDown() throws IOException {
		qosLink.close();
	}

	@Test
	public void canDoGuranteedDelivery() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");
		qosLink = new ConnectionBasedQosLink(connectionTo(arduino),
				ArdulinkProtocol255.instance(), 15, MINUTES);
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void doesThrowExceptionIfNotResponseReceivedWithinHalfAsecond()
			throws Exception {
		qosLink = new ConnectionBasedQosLink(connectionTo(arduino),
				ArdulinkProtocol255.instance(), 500, MILLISECONDS);
		exceptions.expect(IllegalStateException.class);
		exceptions.expectMessage(allOf(containsString("response"),
				containsString("500 MILLISECONDS")));
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void doesThrowExceptionIfKoResponse() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ko?id=%s");
		Connection connection = connectionTo(arduino);
		qosLink = new ConnectionBasedQosLink(connection,
				ArdulinkProtocol255.instance(), 500, MILLISECONDS);
		exceptions.expect(IllegalStateException.class);
		exceptions.expectMessage(allOf(containsString("status"),
				containsString("not ok")));
		qosLink.sendNoTone(analogPin(3));
	}

	@Test
	public void secondCallPassesIfFirstOnKeepsUnresponded() throws Exception {
		arduino.whenReceive(regex("alp:\\/\\/tone\\/4/5/6\\?id\\=(\\d)"))
				.thenDoNotRespond();
		arduino.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");
		qosLink = new ConnectionBasedQosLink(connectionTo(arduino),
				ArdulinkProtocol255.instance(), 500, MILLISECONDS);
		exceptions.expect(IllegalStateException.class);
		exceptions.expectMessage(allOf(containsString("response"),
				containsString("500 MILLISECONDS")));
		qosLink.sendTone(Tone.forPin(analogPin(4)).withHertz(5)
				.withDuration(6, MILLISECONDS));
		exceptions = ExpectedException.none();
		qosLink.sendNoTone(analogPin(3));
	}

	private StreamConnection connectionTo(Arduino arduino) {
		return new StreamConnection(arduino.getInputStream(),
				arduino.getOutputStream(), ArdulinkProtocolN.instance());
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

}
