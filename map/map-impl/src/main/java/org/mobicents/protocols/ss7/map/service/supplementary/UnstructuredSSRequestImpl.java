package org.mobicents.protocols.ss7.map.service.supplementary;

import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;

public class UnstructuredSSRequestImpl extends USSDServiceImpl implements
		UnstructuredSSRequest {

	public UnstructuredSSRequestImpl(byte ussdDataCodingSch, byte[] ussdString) {
		super(ussdDataCodingSch, ussdString);
	}

}