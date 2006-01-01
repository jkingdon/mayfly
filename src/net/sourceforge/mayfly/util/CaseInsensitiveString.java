package net.sourceforge.mayfly.util;

public class CaseInsensitiveString {
    
    private final String delegate;
    
    public CaseInsensitiveString(String contents) {
        this.delegate = contents;
    }

    public String toString() {
        return getString();
    }

    public String getString() {
        return delegate;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof CaseInsensitiveString)) {
            return false;
        }
        
        return delegate.equalsIgnoreCase(((CaseInsensitiveString) obj).getString());
    }
    
    public int hashCode() {
        return delegate.toLowerCase().hashCode();
    }

}
