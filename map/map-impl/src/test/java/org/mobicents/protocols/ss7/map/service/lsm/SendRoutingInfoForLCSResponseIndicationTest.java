/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.ss7.map.service.lsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.map.MapServiceFactoryImpl;
import org.mobicents.protocols.ss7.map.api.MapServiceFactory;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.lsm.LCSLocationInfo;
import org.mobicents.protocols.ss7.map.api.service.lsm.SubscriberIdentity;
import org.mobicents.protocols.ss7.tcap.asn.TcapFactory;
import org.mobicents.protocols.ss7.tcap.asn.comp.Parameter;

/**
 * Trace is from Brazil Operator
 * 
 * @author amit bhayani
 * 
 */
public class SendRoutingInfoForLCSResponseIndicationTest {
	MapServiceFactory mapServiceFactory = new MapServiceFactoryImpl();

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testDecodeProvideSubscriberLocationRequestIndication() throws Exception {
		// The trace is from Brazilian operator
		byte[] data = new byte[] { (byte) 0xa0, 0x09, (byte) 0x81, 0x07, (byte) 0x91, 0x55, 0x16, 0x28, (byte) 0x81, 0x00, 0x70, (byte) 0xa1, 0x07, 0x04, 0x05,
				(byte) 0x91, 0x55, 0x16, 0x09, 0x00 };

		Parameter p = TcapFactory.createParameter();
		p.setPrimitive(false);
		p.setData(data);

		SendRoutingInfoForLCSResponseIndicationImpl rtgInfnoForLCSresInd = new SendRoutingInfoForLCSResponseIndicationImpl();
		rtgInfnoForLCSresInd.decode(p);

		SubscriberIdentity subsIdent = rtgInfnoForLCSresInd.getTargetMS();
		assertNotNull(subsIdent);

		IMSI imsi = subsIdent.getIMSI();
		ISDNAddressString msisdn = subsIdent.getMSISDN();

		assertNotNull(msisdn);
		assertNull(imsi);

		assertEquals(AddressNature.international_number, msisdn.getAddressNature());
		assertEquals(NumberingPlan.ISDN, msisdn.getNumberingPlan());
		assertEquals("556182180007", msisdn.getAddress());
		
		LCSLocationInfo lcsLocInfo = rtgInfnoForLCSresInd.getLCSLocationInfo();
		assertNotNull(lcsLocInfo);

		ISDNAddressString networkNodeNumber = lcsLocInfo.getNetworkNodeNumber();
		assertNotNull(networkNodeNumber);
		assertEquals(AddressNature.international_number, networkNodeNumber.getAddressNature());
		assertEquals(NumberingPlan.ISDN, networkNodeNumber.getNumberingPlan());
		assertEquals("55619000", networkNodeNumber.getAddress());
	}

	@Test
	public void testEncode() throws Exception {
		// The trace is from Brazilian operator
		byte[] data = new byte[] { (byte) 0xa0, 0x09, (byte) 0x81, 0x07, (byte) 0x91, 0x55, 0x16, 0x28, (byte) 0x81, 0x00, 0x70, (byte) 0xa1, 0x07, 0x04, 0x05,
				(byte) 0x91, 0x55, 0x16, 0x09, 0x00 };

		ISDNAddressString msisdn = this.mapServiceFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, "556182180007");
		SubscriberIdentity subsIdent = new SubscriberIdentityImpl(msisdn);

		ISDNAddressString networkNodeNumber = this.mapServiceFactory
				.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, "55619000");

		LCSLocationInfo lcsLocInfo = new LCSLocationInfoImpl(networkNodeNumber, null, null, null, null, null, null);

		SendRoutingInfoForLCSResponseIndicationImpl rtgInfnoForLCSresInd = new SendRoutingInfoForLCSResponseIndicationImpl(subsIdent, lcsLocInfo, null, null,
				null, null, null);

		AsnOutputStream asnOs = new AsnOutputStream();
		rtgInfnoForLCSresInd.encode(asnOs);

		assertTrue(Arrays.equals(data, asnOs.toByteArray()));
	}
}