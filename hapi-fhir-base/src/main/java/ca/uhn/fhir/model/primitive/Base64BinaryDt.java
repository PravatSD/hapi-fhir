package ca.uhn.fhir.model.primitive;

/*
 * #%L
 * HAPI FHIR Library
 * %%
 * Copyright (C) 2014 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.codec.binary.Base64;

import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.SimpleSetter;

@DatatypeDef(name = "base64Binary")
public class Base64BinaryDt extends BasePrimitive<byte[]> {

	private byte[] myValue;

	/**
	 * Constructor
	 */
	public Base64BinaryDt() {
		super();
	}

	/**
	 * Constructor
	 */
	@SimpleSetter
	public Base64BinaryDt(@SimpleSetter.Parameter(name="theBytes") byte[] theBytes) {
		setValue(theBytes);
	}

	@Override
	public void setValueAsString(String theValue) {
		if (theValue == null) {
			myValue = null;
		} else {
			myValue = Base64.decodeBase64(theValue);
		}
	}

	@Override
	public String getValueAsString() {
		if (myValue == null) {
			return null;
		} else {
			return Base64.encodeBase64String(myValue);
		}
	}

	@Override
	public void setValue(byte[] theValue) {
		myValue = theValue;
	}

	@Override
	public byte[] getValue() {
		return myValue;
	}

}
