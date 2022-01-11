package com.starmediadev.data;

/**
 * The different data types for SQL based databases supported by this library
 * This also contains the different Java Classes that they use and is used to verify serialized objects
 */
public enum SQLDataType {
    VARCHAR(String.class), INT(Integer.class, int.class), DOUBLE(Double.class, double.class), BIGINT(Long.class, long.class);
    
    private final Class<?> mainJavaClass;
    private final Class<?>[] additionalJavaClasses;
    
    SQLDataType(Class<?> mainJavaClass) {
        this.mainJavaClass = mainJavaClass;
        this.additionalJavaClasses = null;
    }
    
    SQLDataType(Class<?> mainJavaClass, Class<?>... additionalJavaClasses) {
        this.mainJavaClass = mainJavaClass;
        this.additionalJavaClasses = additionalJavaClasses;
    }
    
    public Class<?> getMainJavaClass() {
        return mainJavaClass;
    }
    
    public Class<?>[] getAdditionalJavaClasses() {
        return additionalJavaClasses;
    }
    
    public boolean isValidType(Class<?> clazz) {
        if (clazz.getName().equals(mainJavaClass.getName())) {
            return true;
        } else {
            for (Class<?> additionalJavaClass : additionalJavaClasses) {
                return clazz.getName().equals(additionalJavaClass.getName());
            }
        }
        
        return false;
    }
    
    public boolean isValidType(Object object) {
        return isValidType(object.getClass());
    }
}
