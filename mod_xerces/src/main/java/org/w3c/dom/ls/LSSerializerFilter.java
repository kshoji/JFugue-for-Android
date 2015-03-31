package org.w3c.dom.ls;

public interface LSSerializerFilter extends org.w3c.dom.traversal.NodeFilter {

    /**
     * Tells the LSSerializer what types of nodes to show to the filter.
     */
    int	getWhatToShow();
}
