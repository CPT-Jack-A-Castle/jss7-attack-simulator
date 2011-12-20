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

package org.mobicents.protocols.ss7.map.smstpdu;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.mobicents.protocols.ss7.map.GSMCharset;
import org.mobicents.protocols.ss7.map.GSMCharsetDecoder;
import org.mobicents.protocols.ss7.map.GSMCharsetDecodingData;
import org.mobicents.protocols.ss7.map.GSMCharsetEncoder;
import org.mobicents.protocols.ss7.map.GSMCharsetEncodingData;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.smstpdu.ConcatenatedMessage;
import org.mobicents.protocols.ss7.map.api.smstpdu.DataCodingScheme;
import org.mobicents.protocols.ss7.map.api.smstpdu.Gsm7NationalLanguageIdentifier;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserData;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserDataHeader;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserDataHeaderElement;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class UserDataImpl implements UserData {

	private static GSMCharset gsm7Charset = new GSMCharset("GSM", new String[] {});
	private static Charset ucs2Charset = Charset.forName("UTF-16BE");

	private DataCodingScheme dataCodingScheme;
	private Charset gsm8Charset;
	private byte[] encodedData;
	private int encodedUserDataLength;
	private boolean encodedUserDataHeaderIndicator;
	private UserDataHeader decodedUserDataHeader;
	private String decodedMessage;

	public UserDataImpl(byte[] encodedData, DataCodingScheme dataCodingScheme, int encodedUserDataLength, boolean encodedUserDataHeaderIndicator, Charset gsm8Charset) {
		this.encodedData = encodedData;
		this.encodedUserDataLength = encodedUserDataLength;
		this.encodedUserDataHeaderIndicator = encodedUserDataHeaderIndicator;
		if (dataCodingScheme != null)
			this.dataCodingScheme = dataCodingScheme;
		else
			this.dataCodingScheme = new DataCodingSchemeImpl(0);
		this.gsm8Charset = gsm8Charset;
	}

	public UserDataImpl(String decodedMessage, DataCodingScheme dataCodingScheme, UserDataHeader decodedUserDataHeader, Charset gsm8Charset) {
		this.decodedMessage = decodedMessage;
		this.decodedUserDataHeader = decodedUserDataHeader;
		if (dataCodingScheme != null)
			this.dataCodingScheme = dataCodingScheme;
		else
			this.dataCodingScheme = new DataCodingSchemeImpl(0);
		this.gsm8Charset = gsm8Charset;
	}

	@Override
	public byte[] getEncodedData() {
		return this.encodedData;
	}

	@Override
	public int getEncodedUserDataLength() {
		return encodedUserDataLength;
	}

	@Override
	public boolean getEncodedUserDataHeaderIndicator() {
		return encodedUserDataHeaderIndicator;
	}

	@Override
	public UserDataHeader getDecodedUserDataHeader() {
		return decodedUserDataHeader;
	}

	@Override
	public String getDecodedString() {
		return decodedMessage;
	}

	@Override
	public void encode() throws MAPException {

		this.encodedData = null;
		this.encodedUserDataLength = 0;
		this.encodedUserDataHeaderIndicator = false;

		if (this.decodedMessage == null)
			this.decodedMessage = "";

		// encoding UserDataHeader if it exists
		byte[] buf2 = null;
		if (this.decodedUserDataHeader != null) {
			buf2 = this.decodedUserDataHeader.getEncodedData();
			if (buf2 != null && buf2.length > 0)
				this.encodedUserDataHeaderIndicator = true;
			else
				buf2 = null;
		}

		if (this.dataCodingScheme.getIsCompressed()) {
			// TODO: implement the case with compressed message
			throw new MAPException("Error encoding a text in Sms UserData: compressed message is not supported yet");
		} else {
			switch (this.dataCodingScheme.getCharacterSet()) {
			case GSM7:
				// selecting a Charset for encoding
				Charset cSet = gsm7Charset;
				Gsm7NationalLanguageIdentifier nationalLanguageLockingShift = null;
				Gsm7NationalLanguageIdentifier nationalLanguageSingleShift = null;
				if (this.decodedUserDataHeader != null) {
					nationalLanguageLockingShift = this.decodedUserDataHeader.getNationalLanguageLockingShift();
					nationalLanguageSingleShift = this.decodedUserDataHeader.getNationalLanguageSingleShift();
				}
				if (nationalLanguageLockingShift != null || nationalLanguageSingleShift != null) {
					// TODO: if nationalLanguageLockingShift or
					// nationalLanguageSingleShift are present set cSet to the
					// National Language GSM7 charset
					// cSet = .........;
				}

				GSMCharsetEncoder encoder = (GSMCharsetEncoder) cSet.newEncoder();
				encoder.setGSMCharsetEncodingData(new GSMCharsetEncodingData(buf2));
				ByteBuffer bb = null;
				try {
					bb = encoder.encode(CharBuffer.wrap(this.decodedMessage));
				} catch (CharacterCodingException e) {
					// This can not occur
				}
				this.encodedUserDataLength = encoder.getGSMCharsetEncodingData().getTotalSeptetCount();
				if (bb != null) {
					this.encodedData = new byte[bb.limit()];
					bb.get(this.encodedData);
				} else {
					this.encodedData = new byte[0];
				}
				break;

			case GSM8:
				if (gsm8Charset != null) {
					bb = gsm8Charset.encode(this.decodedMessage);
					this.encodedData = new byte[bb.limit()];
					bb.get(this.encodedData);
					if (buf2 != null) {
						byte[] tempBuf = this.encodedData;
						this.encodedData = new byte[buf2.length + tempBuf.length];
						System.arraycopy(buf2, 0, this.encodedData, 0, buf2.length);
						System.arraycopy(tempBuf, 0, this.encodedData, buf2.length, tempBuf.length);
					}
					this.encodedUserDataLength = this.encodedData.length;
				} else {
					throw new MAPException("Error encoding a text in Sms UserData: gsm8Charset is not defined for GSM8 dataCodingScheme");
				}
				break;

			case UCS2:
				bb = ucs2Charset.encode(this.decodedMessage);
				this.encodedData = new byte[bb.limit()];
				bb.get(this.encodedData);
				if (buf2 != null) {
					byte[] tempBuf = this.encodedData;
					this.encodedData = new byte[buf2.length + tempBuf.length];
					System.arraycopy(buf2, 0, this.encodedData, 0, buf2.length);
					System.arraycopy(tempBuf, 0, this.encodedData, buf2.length, tempBuf.length);
				}
				this.encodedUserDataLength = this.encodedData.length;
				break;
			}
		}
	}

	@Override
	public void decode() throws MAPException {

		this.decodedUserDataHeader = null;
		this.decodedMessage = null;

		if (this.encodedData == null)
			throw new MAPException("Error decoding a text from Sms UserData: encodedData field is null");

		int offset = 0;
		if (this.encodedUserDataHeaderIndicator) {
			// decode userDataHeader
			if (this.encodedData.length < 1)
				return;
			offset = (this.encodedData[0] & 0xFF) + 1;
			if (offset > this.encodedData.length)
				return;

			this.decodedUserDataHeader = new UserDataHeaderImpl(this.encodedData);
		}

		if (this.dataCodingScheme.getIsCompressed()) {
			// TODO: implement the case with compressed sms message
			// If this is a signal message - this.decodedMessage can be decoded
			// If thus is a segment of concatenated message this.decodedMessage should stay null and this case should be processed by a special static method
			int i1 = 0;
			i1++;
		} else {
			switch (this.dataCodingScheme.getCharacterSet()) {
			case GSM7:
				GSMCharset cSet = gsm7Charset;
				Gsm7NationalLanguageIdentifier nationalLanguageLockingShift = null;
				Gsm7NationalLanguageIdentifier nationalLanguageSingleShift = null;
				if (this.decodedUserDataHeader != null) {
					nationalLanguageLockingShift = this.decodedUserDataHeader.getNationalLanguageLockingShift();
					nationalLanguageSingleShift = this.decodedUserDataHeader.getNationalLanguageSingleShift();
				}
				if (nationalLanguageLockingShift != null || nationalLanguageSingleShift != null) {
					// TODO: if nationalLanguageLockingShift or
					// nationalLanguageSingleShift are present set cSet to the
					// National Language GSM7 charset
					// cSet = .........;
				}

				GSMCharsetDecoder decoder = (GSMCharsetDecoder) cSet.newDecoder();
				if (offset > 0) {
					int bitOffset = offset * 8;
					int septetOffset = (bitOffset - 1) / 7 + 1;
					decoder.setGSMCharsetDecodingData(new GSMCharsetDecodingData(this.encodedUserDataLength, septetOffset));
				} else
					decoder.setGSMCharsetDecodingData(new GSMCharsetDecodingData(this.encodedUserDataLength, 0));
				ByteBuffer bb = ByteBuffer.wrap(this.encodedData);
				CharBuffer bf = null;
				try {
					bf = decoder.decode(bb);
				} catch (CharacterCodingException e) {
					// This can not occur
				}
				if (bf != null)
					this.decodedMessage = bf.toString();
				else
					this.decodedMessage = "";
				break;

			case GSM8:
				if (gsm8Charset != null) {
					byte[] buf = this.encodedData;
					int len = this.encodedUserDataLength;
					if (len > buf.length)
						len = buf.length;
					if (offset > 0) {
						if (offset > len)
							buf = new byte[0];
						else {
							buf = new byte[len - offset];
							System.arraycopy(this.encodedData, offset, buf, 0, len - offset);
						}
					}
					bb = ByteBuffer.wrap(buf);
					bf = gsm8Charset.decode(bb);
					this.decodedMessage = bf.toString();
				}
				break;

			case UCS2:
				byte[] buf = this.encodedData;
				int len = this.encodedUserDataLength;
				if (len > buf.length)
					len = buf.length;
				if (offset > 0) {
					if (offset > len)
						buf = new byte[0];
					else {
						buf = new byte[len - offset];
						System.arraycopy(this.encodedData, offset, buf, 0, len - offset);
					}
				}
				bb = ByteBuffer.wrap(buf);
				bf = ucs2Charset.decode(bb);
				this.decodedMessage = bf.toString();
				break;
			}
		}
	}

	public static UserData[] encodeSplitedMessagesSet(String msg, DataCodingScheme dataCodingScheme,
			Gsm7NationalLanguageIdentifier nationalLanguageLockingShift, Gsm7NationalLanguageIdentifier nationalLanguageSingleShift, boolean referenceIs16bit,
			UserDataHeaderElement[] extraUserDataHeader) throws MAPException {

		// TODO: implement the splitMessage tool: splitting a sms message into several messages depending on data coding
		throw new MAPException("Not yet implemented");
	}

	public static ConcatenatedMessage decodeSplittedMessagesSet(UserData[] encodedArray, DataCodingScheme dataCodingScheme) throws MAPException {

		// TODO: implement the splitMessage tool: decoding splitted sms messages into a solid message
		throw new MAPException("Not yet implemented");
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append("TP-User-Data [");
		if (this.decodedMessage == null) {
			if (this.encodedData != null)
				sb.append(printDataArr(this.encodedData));
		} else {
			sb.append(this.decodedMessage);
			if (this.decodedUserDataHeader != null) {
				sb.append("\n");
				sb.append(this.decodedUserDataHeader.toString());
			}
		}
		sb.append("]");

		return sb.toString();
	}

	private String printDataArr(byte[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int b : arr) {
			sb.append(b);
			sb.append(", ");
		}

		return sb.toString();
	}
}
