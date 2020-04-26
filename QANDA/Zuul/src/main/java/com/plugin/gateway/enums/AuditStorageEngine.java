/**
 * 
 */
package com.plugin.gateway.enums;

/**
 * @author Sankha
 *
 */
public enum AuditStorageEngine {

	FILE("file"),MONGODB("mongodb"),FILESYSTEM("filesystem"),MONGO("mongo");
	String value;


    public String getValue() {
        return value;
    }

    private AuditStorageEngine(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return value;
    }
}
