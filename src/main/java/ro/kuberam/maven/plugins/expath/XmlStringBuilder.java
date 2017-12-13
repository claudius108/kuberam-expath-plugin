package ro.kuberam.maven.plugins.expath;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.nio.charset.Charset;
import java.util.*;

import static ro.kuberam.maven.plugins.expath.XmlStringBuilder.State.*;

/**
 * A simple wrapper around {@link StringBuilder} which
 * presents a builder interface for creating XML Documents.
 *
 * Enforces some basic sanity checks to ensure the created document
 * is well-formed XML. Also tries to manage namespaces declarations.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 * @version 1.0
 */
public class XmlStringBuilder {

    private static final String DEFAULT_INDENT = "    ";  // 4 spaces
    private static final String EOL = System.getProperty("line.separator");

    private final boolean indenting;
    private final String indent;
    private final StringBuilder buf = new StringBuilder();
    private final Deque<QName> elementTree = new ArrayDeque<>();
    private String defaultNs = XMLConstants.NULL_NS_URI;
    private final Map<String, String> inscopeNamespaces = new HashMap<>();
    private int genNsIdx = 0;
    private State currentState = null;
    private State previousState = null;

    public XmlStringBuilder() {
        this(true);
    }

    public XmlStringBuilder(final boolean indenting) {
        this(indenting, DEFAULT_INDENT);
    }

    public XmlStringBuilder(final boolean indenting, final String indent) {
        this.indenting = indenting;
        this.indent = indent;
    }

    protected enum State {
        START_DOCUMENT,
        XML_DECLARATION,
        START_ELEMENT,
        TEXT,
        END_ELEMENT,
        END_DOCUMENT
    }

    private final static Map<State, EnumSet<State>> stateTransistions = Collections.unmodifiableMap(
      new HashMap<State, EnumSet<State>>() {{
          put(START_DOCUMENT, EnumSet.of(XML_DECLARATION, START_ELEMENT, END_DOCUMENT));
          put(XML_DECLARATION, EnumSet.of(START_ELEMENT, END_DOCUMENT));
          put(START_ELEMENT, EnumSet.of(START_ELEMENT, TEXT, END_ELEMENT, END_DOCUMENT));
          put(TEXT, EnumSet.of(TEXT, START_ELEMENT, END_ELEMENT));
          put(END_ELEMENT, EnumSet.of(TEXT, END_ELEMENT, START_ELEMENT, END_DOCUMENT));
          put(END_DOCUMENT, EnumSet.noneOf(State.class));
      }}
    );

    private void checkValidState(final State newState) throws IllegalArgumentException {
        if(currentState != null) {
            final EnumSet<State> validNewStates = stateTransistions.get(currentState);
            if(!validNewStates.contains(newState)) {
                throw new IllegalStateException("Not allowed to transition from: " + currentState.name() + ", to: " + newState.name());
            }
            previousState = currentState;
        }

        currentState = newState;
    }

    public XmlStringBuilder startDocument() {
        checkValidState(START_DOCUMENT);
        return this;
    }

    public XmlStringBuilder xmlDeclaration(final String version, final String encoding) {
        checkValidState(XML_DECLARATION);
        buf.append("<?xml version=\"").append(version).append("\" encoding=\"").append(encoding).append("\"?>");
        bufEol();
        return this;
    }

    public XmlStringBuilder xmlDeclaration(final String version, final Charset encoding) {
        return xmlDeclaration(version, encoding.displayName());
    }

    public XmlStringBuilder startElement(final QName qname, final Attribute... attributes) {
        checkValidState(START_ELEMENT);

        if(START_ELEMENT == previousState || END_ELEMENT == previousState) {
            bufEol();
            indentBuf();
        }

        buf.append('<');
        final String ns = qname.getNamespaceURI();
        if(ns == null || ns.isEmpty() || defaultNs == ns) {
            buf.append(qname.getLocalPart());
        } else {
            if(defaultNs == XMLConstants.NULL_NS_URI) {
                buf.append(qname.getLocalPart());
                buf.append(" xmlns").append("=\"").append(ns).append("\"");

                defaultNs = ns; // memomize
            } else {
                final String knownPrefix = inscopeNamespaces.get(ns);
                if (knownPrefix != null) {
                    buf.append(knownPrefix).append(':').append(qname.getLocalPart());
                } else {
                    final String providedPrefix = qname.getPrefix();
                    if (providedPrefix != null && !providedPrefix.isEmpty()) {
                        buf.append(providedPrefix).append(':').append(qname.getLocalPart());
                        inscopeNamespaces.put(ns, providedPrefix); // memoize the provided ns and prefix

                        // write namespace decl
                        buf.append(" xmlns:").append(providedPrefix).append("=\"").append(ns).append("\"");

                    } else {
                        // else generate prefix
                        final String generatedPrefix = "ns" + genNsIdx++;
                        buf.append(generatedPrefix).append(':').append(qname.getLocalPart());
                        inscopeNamespaces.put(ns, generatedPrefix); // memoize the generated ns and prefix

                        // write namespace decl
                        buf.append(" xmlns:").append(generatedPrefix).append("=\"").append(ns).append("\"");
                    }
                }
            }
        }

        for(final Attribute attr : attributes) {
            //TODO(AR) add qname handling for attributes
            buf.append(' ').append(attr.getName().getLocalPart()).append("=\"").append(attr.getValue()).append("\"");
        }

        buf.append(">");

        elementTree.push(qname);

        return this;
    }

    public XmlStringBuilder text(final CharSequence chars) {
        checkValidState(TEXT);

        buf.append(chars);

        return this;
    }

    public XmlStringBuilder endElement(final QName qname) {
        checkValidState(END_ELEMENT);

        final QName prevElementStart = elementTree.peek();
        if(!qname.equals(prevElementStart)) {
            throw new IllegalStateException("Previously started an element '" + prevElementStart + "', but trying to close element '" + qname + "'");
        }

        elementTree.pop();

        if(END_ELEMENT == previousState) {
            bufEol();
            indentBuf();
        }

        //TODO(AR) better handling of inScopeNamespaces, we probably need to pop them or something

        buf.append("</");
        final String ns = qname.getNamespaceURI();
        if(ns == null || ns.isEmpty() || defaultNs == ns) {
            buf.append(qname.getLocalPart());
        } else {
            final String knownPrefix = inscopeNamespaces.get(ns);
            if(knownPrefix != null) {
                buf.append(knownPrefix).append(':').append(qname.getLocalPart());
            } else {
                throw new IllegalStateException("Cannot find prefix for: " + qname);
            }
        }
        buf.append('>');

        return this;
    }

    public XmlStringBuilder endDocument() {
        checkValidState(END_DOCUMENT);

        return this;
    }

    public String build() {
        if(END_DOCUMENT != currentState) {
            throw new IllegalStateException("endDocument has not yet been called");
        }

        return buf.toString();
    }

    public void reset() {
        buf.setLength(0);
        elementTree.clear();
        defaultNs = XMLConstants.NULL_NS_URI;
        inscopeNamespaces.clear();
        genNsIdx = 0;
        currentState = null;
        previousState = null;
    }

    private void indentBuf() {
        if(indenting) {
            for(int i = 0; i < elementTree.size(); i++) {
                buf.append(indent);
            }
        }
    }

    private void bufEol() {
        if(indenting) {
            buf.append(EOL);
        }
    }

    public static class Attribute {
        private final QName name;
        private final String value;

        public Attribute(final QName name, final String value) {
            this.name = name;
            this.value = value;
        }

        public QName getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
