/*
 * JFugue, an Application Programming Interface (API) for Music Programming
 * http://www.jfugue.org
 *
 * Copyright (C) 2003-2014 David Koelle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfugue.parser;

public class ParserException extends RuntimeException 
{
	private static final long serialVersionUID = -5224628162824708074L;

	private String exception;
	private String errantString;
	private int position = -1;
	
	public ParserException(String exception, String errantString) {
		super(exception+ ": "+errantString);
		this.exception = exception;
		this.errantString = errantString;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	public String getMessage() {
		if (position > -1) {
			return new String(this.exception + ": "+errantString+" (Position "+position+")");
		} else {
			return new String(this.exception + ": "+errantString);
		}
	}
}
