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

package org.staccato;

import java.util.LinkedList;
import java.util.List;

import org.jfugue.parser.Parser;
import org.jfugue.parser.ParserException;
import org.jfugue.pattern.PatternProducer;
import org.staccato.functions.ArpeggiatedChordFunction;
import org.staccato.functions.ChannelPressureFunction;
import org.staccato.functions.ControllerFunction;
import org.staccato.functions.DefaultPreprocessorFunction;
import org.staccato.functions.FunctionManager;
import org.staccato.functions.PitchWheelFunction;
import org.staccato.functions.PolyPressureFunction;
import org.staccato.functions.SysexFunction;
import org.staccato.functions.TrillFunction;

public class StaccatoParser extends Parser {
	private List<Preprocessor> preprocessors;
	private List<Subparser> subparsers;
	private StaccatoParserContext context;
	private boolean throwExceptionOnUnknownToken = false;
	
	public StaccatoParser() {
		super();
		context = new StaccatoParserContext(this);

        NoteSubparser.populateContext(context);
        TempoSubparser.populateContext(context);
        IVLSubparser.populateContext(context);
		
        FunctionManager funMan = FunctionManager.getInstance();
        funMan.addPreprocessorFunction(DefaultPreprocessorFunction.getInstance());
        funMan.addPreprocessorFunction(TrillFunction.getInstance());
        funMan.addPreprocessorFunction(ArpeggiatedChordFunction.getInstance());
        funMan.addSubparserFunction(PitchWheelFunction.getInstance());
        funMan.addSubparserFunction(ControllerFunction.getInstance());
        funMan.addSubparserFunction(ChannelPressureFunction.getInstance());
        funMan.addSubparserFunction(PolyPressureFunction.getInstance());
        funMan.addSubparserFunction(SysexFunction.getInstance());
		
		preprocessors = new LinkedList<Preprocessor>();
        preprocessors.add(ReplacementMapPreprocessor.getInstance());
        preprocessors.add(InstructionPreprocessor.getInstance());
		preprocessors.add(UppercasePreprocessor.getInstance());
		preprocessors.add(CollectedNotesPreprocessor.getInstance());
		preprocessors.add(ParenSpacesPreprocessor.getInstance());
		preprocessors.add(FunctionPreprocessor.getInstance());
		preprocessors.add(MicrotonePreprocessor.getInstance());
		
		subparsers = new LinkedList<Subparser>();
		subparsers.add(NoteSubparser.getInstance());
		subparsers.add(BarLineSubparser.getInstance());
		subparsers.add(IVLSubparser.getInstance());
		subparsers.add(SignatureSubparser.getInstance());
		subparsers.add(TempoSubparser.getInstance());
		subparsers.add(BeatTimeSubparser.getInstance());
		subparsers.add(LyricMarkerSubparser.getInstance());
		subparsers.add(FunctionSubparser.getInstance());
	}
	
	public void setThrowsExceptionOnUnknownToken(boolean b) {
		this.throwExceptionOnUnknownToken = b;
	}
	
	public boolean throwsExceptionOnUnknownToken() {
		return this.throwExceptionOnUnknownToken;
	}
	
	public void parse(PatternProducer patternProducer) {
		parse(patternProducer.getPattern().toString());
	}

	public String preprocess(PatternProducer p) {
		return preprocess(p.toString());
	}
	
	public String preprocess(String s) {
		for (Preprocessor pre : preprocessors) {
			s = pre.preprocess(s, context);
		}
		return s;
	}

	public void parse(String s) {
		fireBeforeParsingStarts();

		// First, run the pre-processors
		s = preprocess(s);

		// Now, parse the music
		String[] strings = s.split(" ");
		for (String substring : strings) {
			if (!substring.isEmpty()) {
				boolean matchingSubparserFound = false;
				for (Subparser sub : subparsers) {
					if (!matchingSubparserFound && sub.matches(substring)) {
						sub.parse(substring, context);
						matchingSubparserFound = true;
					}
				}
				if (!matchingSubparserFound) {
					if (throwsExceptionOnUnknownToken()) {
						throw new ParserException(StaccatoMessages.NO_PARSER_FOUND, substring);
					}
				}
			}
		}
		
		fireAfterParsingFinished();
	}
}
