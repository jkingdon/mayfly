package net.sourceforge.mayfly.ldbc;

/**
 * Emulate the Java 1.5 StringBuilder class, so that when we switch to 1.5 we don't
 * need to find and replace StringBuffer all through the code.
 */

// TODO: Move this to util package.

public class StringBuilder {
    
    private StringBuffer delegate;

    public StringBuilder() {
        delegate = new StringBuffer();
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public StringBuilder append(Object obj) {
        delegate.append(obj);
        return this;
    }

    public StringBuilder append(boolean bool) {
        delegate.append(bool);
        return this;
    }

    public StringBuilder append(char ch) {
        delegate.append(ch);
        return this;
    }

    public StringBuilder append(char[] data) {
        delegate.append(data);
        return this;
    }

    public StringBuilder append(char[] data, int offset, int count) {
        delegate.append(data, offset, count);
        return this;
    }

    public StringBuilder append(double dnum) {
        delegate.append(dnum);
        return this;
    }

    public StringBuilder append(float fnum) {
        delegate.append(fnum);
        return this;
    }

    public StringBuilder append(int inum) {
        delegate.append(inum);
        return this;
    }

    public StringBuilder append(long lnum) {
        delegate.append(lnum);
        return this;
    }

    public int capacity() {
        return delegate.capacity();
    }

    public char charAt(int index) {
        return delegate.charAt(index);
    }

    public StringBuilder delete(int start, int end) {
        delegate.delete(start, end);
        return this;
    }

    public StringBuilder deleteCharAt(int index) {
        delegate.deleteCharAt(index);
        return this;
    }

    public void ensureCapacity(int minimumCapacity) {
        delegate.ensureCapacity(minimumCapacity);
    }

    public void getChars(int srcOffset, int srcEnd, char[] dst, int dstOffset) {
        delegate.getChars(srcOffset, srcEnd, dst, dstOffset);
    }

    public int indexOf(String str) {
        return delegate.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return delegate.indexOf(str, fromIndex);
    }

    public StringBuilder insert(int offset, Object obj) {
        delegate.insert(offset, obj);
        return this;
    }

    public StringBuilder insert(int offset, String str) {
        delegate.insert(offset, str);
        return this;
    }

    public StringBuilder insert(int offset, boolean bool) {
        delegate.insert(offset, bool);
        return this;
    }

    public StringBuilder insert(int offset, char ch) {
        delegate.insert(offset, ch);
        return this;
    }

    public StringBuilder insert(int offset, char[] data) {
        delegate.insert(offset, data);
        return this;
    }

    public StringBuilder insert(int offset, char[] str, int str_offset, int len) {
        delegate.insert(offset, str, str_offset, len);
        return this;
    }

    public StringBuilder insert(int offset, double dnum) {
        delegate.insert(offset, dnum);
        return this;
    }

    public StringBuilder insert(int offset, float fnum) {
        delegate.insert(offset, fnum);
        return this;
    }

    public StringBuilder insert(int offset, int inum) {
        delegate.insert(offset, inum);
        return this;
    }

    public StringBuilder insert(int offset, long lnum) {
        delegate.insert(offset, lnum);
        return this;
    }

    public int lastIndexOf(String str) {
        return delegate.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return delegate.lastIndexOf(str, fromIndex);
    }

    public int length() {
        return delegate.length();
    }

    public StringBuilder replace(int start, int end, String str) {
        delegate.replace(start, end, str);
        return this;
    }

    public StringBuilder reverse() {
        delegate.reverse();
        return this;
    }

    public void setCharAt(int index, char ch) {
        delegate.setCharAt(index, ch);
    }

    public void setLength(int newLength) {
        delegate.setLength(newLength);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return delegate.subSequence(beginIndex, endIndex);
    }

    public String substring(int beginIndex) {
        return delegate.substring(beginIndex);
    }

    public String substring(int beginIndex, int endIndex) {
        return delegate.substring(beginIndex, endIndex);
    }

    public String toString() {
        return delegate.toString();
    }
    
    

}
