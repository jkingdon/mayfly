package net.sourceforge.mayfly.util;

public class CaseInsensitiveString {
    
    private final String delegate;
    private final int hashCode;
    
    public CaseInsensitiveString(String contents) {
        this.delegate = contents;
        if (contents == null) {
            throw new NullPointerException();
        }
        this.hashCode = delegate.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return getString();
    }

    public String getString() {
        return delegate;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CaseInsensitiveString)) {
            return false;
        }
        
        return delegate.equalsIgnoreCase(((CaseInsensitiveString) obj).getString());
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }

}
