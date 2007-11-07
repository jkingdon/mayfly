package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.parser.Location;

public class Position {
    
    public static final Position FIRST = new Position() {
        @Override
        public boolean isFirst() {
            return true;
        }
    };

    public static final Position LAST = new Position() {
        @Override
        public boolean isLast() {
            return true;
        }
    };
    
    public static Position after(String existingName) {
        return after(existingName, Location.UNKNOWN);
    }

    public static Position after(final String existingName, 
        final Location location) {
        return new Position() {
            @Override
            public boolean isAfter(String candidate) {
                return existingName.equalsIgnoreCase(candidate);
            }
            
            @Override
            public String afterWhat() {
                return existingName;
            }
            
            @Override
            public Location location() {
                return location;
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
    
    public Location location() {
        return Location.UNKNOWN;
    }

}
