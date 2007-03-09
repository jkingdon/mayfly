package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyInternalException;

public class Position {
    
    public static final Position FIRST = new Position() {
        public boolean isFirst() {
            return true;
        }
    };

    public static final Position LAST = new Position() {
        public boolean isLast() {
            return true;
        }
    };

    public static Position after(final String existingName) {
        return new Position() {
            public boolean isAfter(String candidate) {
                return existingName.equalsIgnoreCase(candidate);
            }
            
            public String afterWhat() {
                return existingName;
            }
        };
    }

    public boolean isFirst() {
        return false;
    }

    public boolean isLast() {
        return false;
    }

    public boolean isAfter(String existingName) {
        return false;
    }

    public String afterWhat() {
        throw new MayflyInternalException("shouldn't need what for first/last");
    }

}
