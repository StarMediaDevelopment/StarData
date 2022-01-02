package com.starmediadev.data;

/**
 * This is for easier implementation of IDocument. This should be used with custom types that do not need a parent class, or is the parent of an inheritance tree
 * The methods in IDocument are declared as final methods to prevent them from being overridden
 */
public abstract class AbstractDocument implements IDocument {
    
    protected long id;
    
    public final long getId() {
        return id;
    }
    
    public final void setId(long id) {
        this.id = id;
    }
}
