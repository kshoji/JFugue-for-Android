/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.dom.ls;

import org.w3c.dom.events.Event;

/** 
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 *  This interface represents a progress event object that notifies the 
 * application about progress as a document is parsed. It extends the 
 * <code>Event</code> interface defined in .
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-LS-20020725'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface LSProgressEvent extends Event {
    /**
     * The input source that is being parsed.
     */
    public DOMInputSource getInputSource();

    /**
     * The current position in the input source, including all external 
     * entities and other resources that have been read.
     */
    public int getPosition();

    /**
     * The total size of the document including all external resources, this 
     * number might change as a document is being parsed if references to 
     * more external resources are seen.
     */
    public int getTotalSize();

}
