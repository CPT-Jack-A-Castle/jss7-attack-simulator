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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.protocols.ss7.map.MapServiceFactoryImpl;
import org.mobicents.protocols.ss7.map.api.MapServiceFactory;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.lsm.AdditionalNumber;
import org.mobicents.protocols.ss7.tcap.asn.TcapFactory;
import org.mobicents.protocols.ss7.tcap.asn.comp.Parameter;

/**
 * @author amit bhayani
 * 
 */
public class AdditionalNumberTest {
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

	//@Test
	public void testDecode() throws Exception {
		byte[] data = new byte[] { (byte) 0x80, 0x05, (byte)0x91, (byte) 0x55, 0x16, 0x09, 0x70, };

		Parameter p = TcapFactory.createParameter();
		p.setPrimitive(false);
		p.setData(data);

		AdditionalNumber addNum = new AdditionalNumberImpl();
		addNum.decode(p);
		ISDNAddressString isdnAdd = addNum.getMSCNumber(); 
		assertNotNull(isdnAdd);
		
		assertEquals(AddressNature.international_number, isdnAdd.getAddressNature());
		assertEquals(NumberingPlan.ISDN, isdnAdd.getNumberingPlan());

	}

	@Test
	public void testEncode() throws Exception {
		byte[] data = new byte[] { (byte) 0x80, 0x05, (byte)0x91, (byte) 0x55, 0x16, 0x09, 0x70, };
		
		ISDNAddressString isdnAdd = mapServiceFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, "55619007");
		AdditionalNumber addNum = new AdditionalNumberImpl(isdnAdd, null);
		Parameter p = addNum.encode();

		assertTrue(Arrays.equals(data, p.getData()));
	}
}