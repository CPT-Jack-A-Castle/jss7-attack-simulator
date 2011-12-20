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

package org.mobicents.protocols.ss7.map;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.BitSet;

/**
 * 
 * @author amit bhayani
 * @author sergey vetyutnev
 * 
 */
public class GSMCharsetEncoder extends CharsetEncoder {

	private	int bitpos = 0;
	private	int carryOver;
	private	GSMCharset cs;
	private GSMCharsetEncodingData encodingData;

	// The mask to check if corresponding bit in read byte is 1 or 0 and hence
	// store it i BitSet accordingly
	byte[] mask = new byte[] { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40 };

	// BitSet to hold the bits of passed char to be encoded
	BitSet bitSet = new BitSet();

	static final byte ESCAPE = 0x1B;

	protected GSMCharsetEncoder(Charset cs, float averageBytesPerChar,
			float maxBytesPerChar) {
		super(cs, averageBytesPerChar, maxBytesPerChar);
		implReset();
		this.cs = (GSMCharset) cs;

		if (encodingData != null)
			encodingData.totalSeptetCount = 0;
	}

	public void setGSMCharsetEncodingData(GSMCharsetEncodingData encodingData) {
		this.encodingData = encodingData;
	}

	public GSMCharsetEncodingData getGSMCharsetEncodingData() {
		return this.encodingData;
	}

	@Override
	protected void implReset() {
		bitpos = 0;
		carryOver = 0;
		bitSet.clear();
	}

	/**
	 * TODO :
	 */
	@Override
	protected CoderResult implFlush(ByteBuffer out) {

		if (!out.hasRemaining()) {
			return CoderResult.OVERFLOW;
		}
		return CoderResult.UNDERFLOW;
	}

	byte rawData = 0;

	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {

		if (this.encodingData != null && this.encodingData.leadingBuffer != null) {
			int septetCount = (this.encodingData.leadingBuffer.length * 8 + 6) / 7;
			bitpos = septetCount % 8;
			this.encodingData.totalSeptetCount = septetCount; 
			for (int ind = 0; ind < this.encodingData.leadingBuffer.length; ind++) {
				out.put(this.encodingData.leadingBuffer[ind]);
			}
		}
		
		while (in.hasRemaining()) {

			// Read the first char
			char c = in.get();

			boolean found = false;
			// searching a char in the main character table
			for (int i = 0; i < this.cs.mainTable.length; i++) {
				if (this.cs.mainTable[i] == c) {
					found = true;
					this.putByte(i, out);
					break;
				}
			}
			
			// searching a char in the extension character table
			if (!found && this.cs.extensionTable != null) {
				for (int i = 0; i < this.cs.mainTable.length; i++) {
					if (this.cs.extensionTable[i] == c) {
						found = true;
						this.putByte(GSMCharsetEncoder.ESCAPE, out);
						this.putByte(i, out);
						break;
					}
				}
			}

			if (!found) {
				// found no suitable symbol - encode a space char
				this.putByte(0x20, out);
			}
		}
		
		if (bitpos != 0) {
			// writing a carryOver data
			out.put((byte) carryOver);
		}

		return CoderResult.UNDERFLOW;
	}
	
	private void putByte(int data, ByteBuffer out) {

		if (bitpos == 0) {
			carryOver = data;
		} else {
			int i1 = data << (8 - bitpos);
			out.put((byte) (i1 | carryOver));
			carryOver = data >>> bitpos;
		}

		bitpos++;
		if (bitpos == 8) {
			bitpos = 0;
		}

		if (this.encodingData != null)
			this.encodingData.totalSeptetCount++;
	}
}
