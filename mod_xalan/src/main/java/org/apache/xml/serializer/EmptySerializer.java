/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
/*
 * $Id: EmptySerializer.java,v 1.11 2005/04/07 04:29:03 minchau Exp $
 */
package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class is an adapter class. Its only purpose is to be extended and
 * for that extended class to over-ride all methods that are to be used. 
 * 
 * This class is not a public API, it is only public because it is used
 * across package boundaries.
 * 
 * @xsl.usage internal
 */
public class EmptySerializer implements SerializationHandler
{
    protected static final String ERR = "EmptySerializer method not over-ridden";
    /**
     * @see SerializationHandler#asContentHandler()
     */
    
    protected void couldThrowIOException() throws IOException
    {
        return; // don't do anything.
    }
    
    protected void couldThrowSAXException() throws SAXException
    {
        return; // don't do anything.
    }
    
    protected void couldThrowSAXException(char[] chars, int off, int len) throws SAXException
    {
        return; // don't do anything.
    }
    
    protected void couldThrowSAXException(String elemQName) throws SAXException
    {
        return; // don't do anything.
    }
    
    protected void couldThrowException() throws Exception
    {
        return; // don't do anything.
    }

    void aMethodIsCalled()
    {

        // throw new RuntimeException(err);
        return;
    }
  
    
    /**
     * @see SerializationHandler#asContentHandler()
     */
    public ContentHandler asContentHandler() throws IOException
    {
        couldThrowIOException();
        return null;
    }
    /**
     * @see SerializationHandler#setContentHandler(org.xml.sax.ContentHandler)
     */
    public void setContentHandler(ContentHandler ch)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#close()
     */
    public void close()
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#getOutputFormat()
     */
    public Properties getOutputFormat()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see SerializationHandler#getOutputStream()
     */
    public OutputStream getOutputStream()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see SerializationHandler#getWriter()
     */
    public Writer getWriter()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see SerializationHandler#reset()
     */
    public boolean reset()
    {
        aMethodIsCalled();
        return false;
    }
    /**
     * @see SerializationHandler#serialize(org.w3c.dom.Node)
     */
    public void serialize(Node node) throws IOException
    {
        couldThrowIOException();
    }
    /**
     * @see SerializationHandler#setCdataSectionElements(java.util.Vector)
     */
    public void setCdataSectionElements(Vector URI_and_localNames)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setEscaping(boolean)
     */
    public boolean setEscaping(boolean escape) throws SAXException
    {
        couldThrowSAXException();
        return false;
    }
    /**
     * @see SerializationHandler#setIndent(boolean)
     */
    public void setIndent(boolean indent)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setIndentAmount(int)
     */
    public void setIndentAmount(int spaces)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setOutputFormat(java.util.Properties)
     */
    public void setOutputFormat(Properties format)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream output)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setVersion(String)
     */
    public void setVersion(String version)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setWriter(java.io.Writer)
     */
    public void setWriter(Writer writer)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#setTransformer(javax.xml.transform.Transformer)
     */
    public void setTransformer(Transformer transformer)
    {
        aMethodIsCalled();
    }
    /**
     * @see SerializationHandler#getTransformer()
     */
    public Transformer getTransformer()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see SerializationHandler#flushPending()
     */
    public void flushPending() throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#addAttribute(String, String, String, String, String)
     */
    public void addAttribute(
        String uri,
        String localName,
        String rawName,
        String type,
        String value,
        boolean XSLAttribute)        
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#addAttributes(org.xml.sax.Attributes)
     */
    public void addAttributes(Attributes atts) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#addAttribute(String, String)
     */
    public void addAttribute(String name, String value)
    {
        aMethodIsCalled();
    }

    /**
     * @see ExtendedContentHandler#characters(String)
     */
    public void characters(String chars) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#endElement(String)
     */
    public void endElement(String elemName) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#startDocument()
     */
    public void startDocument() throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#startElement(String, String, String)
     */
    public void startElement(String uri, String localName, String qName)
        throws SAXException
    {
        couldThrowSAXException(qName);
    }
    /**
     * @see ExtendedContentHandler#startElement(String)
     */
    public void startElement(String qName) throws SAXException
    {
        couldThrowSAXException(qName);
    }
    /**
     * @see ExtendedContentHandler#namespaceAfterStartElement(String, String)
     */
    public void namespaceAfterStartElement(String uri, String prefix)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#startPrefixMapping(String, String, boolean)
     */
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws SAXException
    {
        couldThrowSAXException();
        return false;
    }
    /**
     * @see ExtendedContentHandler#entityReference(String)
     */
    public void entityReference(String entityName) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedContentHandler#getNamespaceMappings()
     */
    public NamespaceMappings getNamespaceMappings()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see ExtendedContentHandler#getPrefix(String)
     */
    public String getPrefix(String uri)
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see ExtendedContentHandler#getNamespaceURI(String, boolean)
     */
    public String getNamespaceURI(String name, boolean isElement)
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see ExtendedContentHandler#getNamespaceURIFromPrefix(String)
     */
    public String getNamespaceURIFromPrefix(String prefix)
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator arg0)
    {
        aMethodIsCalled();
    }
    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String arg0, String arg1)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, org.xml.sax.Attributes)
     */
    public void startElement(
        String arg0,
        String arg1,
        String arg2,
        Attributes arg3)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String arg0, String arg1, String arg2)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException
    {
        couldThrowSAXException(arg0, arg1, arg2);
    }
    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String arg0, String arg1)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see ExtendedLexicalHandler#comment(String)
     */
    public void comment(String comment) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#startDTD(String, String, String)
     */
    public void startDTD(String arg0, String arg1, String arg2)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(String)
     */
    public void startEntity(String arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(String)
     */
    public void endEntity(String arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] arg0, int arg1, int arg2) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see XSLOutputAttributes#getDoctypePublic()
     */
    public String getDoctypePublic()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see XSLOutputAttributes#getDoctypeSystem()
     */
    public String getDoctypeSystem()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see XSLOutputAttributes#getEncoding()
     */
    public String getEncoding()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see XSLOutputAttributes#getIndent()
     */
    public boolean getIndent()
    {
        aMethodIsCalled();
        return false;
    }
    /**
     * @see XSLOutputAttributes#getIndentAmount()
     */
    public int getIndentAmount()
    {
        aMethodIsCalled();
        return 0;
    }
    /**
     * @see XSLOutputAttributes#getMediaType()
     */
    public String getMediaType()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see XSLOutputAttributes#getOmitXMLDeclaration()
     */
    public boolean getOmitXMLDeclaration()
    {
        aMethodIsCalled();
        return false;
    }
    /**
     * @see XSLOutputAttributes#getStandalone()
     */
    public String getStandalone()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see XSLOutputAttributes#getVersion()
     */
    public String getVersion()
    {
        aMethodIsCalled();
        return null;
    }
    /**
     * @see XSLOutputAttributes#setCdataSectionElements
     */
    public void setCdataSectionElements(Hashtable h) throws Exception
    {
        couldThrowException();
    }
    /**
     * @see XSLOutputAttributes#setDoctype(String, String)
     */
    public void setDoctype(String system, String pub)
    {
        aMethodIsCalled();
    }
    /**
     * @see XSLOutputAttributes#setDoctypePublic(String)
     */
    public void setDoctypePublic(String doctype)
    {
        aMethodIsCalled();
    }
    /**
     * @see XSLOutputAttributes#setDoctypeSystem(String)
     */
    public void setDoctypeSystem(String doctype)
    {
        aMethodIsCalled();
    }
    /**
     * @see XSLOutputAttributes#setEncoding(String)
     */
    public void setEncoding(String encoding)
    {
        aMethodIsCalled();
    }
    /**
     * @see XSLOutputAttributes#setMediaType(String)
     */
    public void setMediaType(String mediatype)
    {
        aMethodIsCalled();
    }
    /**
     * @see XSLOutputAttributes#setOmitXMLDeclaration(boolean)
     */
    public void setOmitXMLDeclaration(boolean b)
    {
        aMethodIsCalled();
    }
    /**
     * @see XSLOutputAttributes#setStandalone(String)
     */
    public void setStandalone(String standalone)
    {
        aMethodIsCalled();
    }
    /**
     * @see org.xml.sax.ext.DeclHandler#elementDecl(String, String)
     */
    public void elementDecl(String arg0, String arg1) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.DeclHandler#attributeDecl(String, String, String, String, String)
     */
    public void attributeDecl(
        String arg0,
        String arg1,
        String arg2,
        String arg3,
        String arg4)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.DeclHandler#internalEntityDecl(String, String)
     */
    public void internalEntityDecl(String arg0, String arg1)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ext.DeclHandler#externalEntityDecl(String, String, String)
     */
    public void externalEntityDecl(String arg0, String arg1, String arg2)
        throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException arg0) throws SAXException
    {
        couldThrowSAXException();
    }
    /**
     * @see Serializer#asDOMSerializer()
     */
    public DOMSerializer asDOMSerializer() throws IOException
    {
        couldThrowIOException();
        return null;
    }

    /**
     * @see SerializationHandler#setNamespaceMappings(NamespaceMappings)
     */
    public void setNamespaceMappings(NamespaceMappings mappings) {
        aMethodIsCalled();
    }
    
    /**
     * @see ExtendedContentHandler#setSourceLocator(javax.xml.transform.SourceLocator)
     */
    public void setSourceLocator(SourceLocator locator)
    {
        aMethodIsCalled();
    }

    /**
     * @see ExtendedContentHandler#addUniqueAttribute(String, String, int)
     */
    public void addUniqueAttribute(String name, String value, int flags)
        throws SAXException
    {
        couldThrowSAXException();
    }

    /**
     * @see ExtendedContentHandler#characters(org.w3c.dom.Node)
     */
    public void characters(Node node) throws SAXException
    {
        couldThrowSAXException();        
    }
    
    /**
     * @see ExtendedContentHandler#addXSLAttribute(String, String, String)
     */
    public void addXSLAttribute(String qName, String value, String uri)
    {
        aMethodIsCalled();        
    }

    /**
     * @see ExtendedContentHandler#addAttribute(String, String, String, String, String)
     */
    public void addAttribute(String uri, String localName, String rawName, String type, String value) throws SAXException 
    {
        couldThrowSAXException();        
    }
    /**
     * @see org.xml.sax.DTDHandler#notationDecl(String, String, String)
     */
    public void notationDecl(String arg0, String arg1, String arg2) throws SAXException 
    {
        couldThrowSAXException(); 
    }

    /**
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl(String, String, String, String)
     */
    public void unparsedEntityDecl(
        String arg0,
        String arg1,
        String arg2,
        String arg3)
        throws SAXException {
        couldThrowSAXException();
    }

    /**
     * @see SerializationHandler#setDTDEntityExpansion(boolean)
     */
    public void setDTDEntityExpansion(boolean expand) {
        aMethodIsCalled();

    }
}
