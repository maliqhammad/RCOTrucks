package com.rco.rcotrucks.activities.forms;


import java.io.Serializable;

public class FormField implements Serializable {

    private String format;
    private String key;
    private String value= "";
    private String scriptKey ="";

    public FormField(String format, String key, String scriptKey, String value) {
        this.format = format;
        this.key = key;
        this.value = value;
        this.scriptKey = scriptKey;
    }

    public FormField(String format, String key, String scriptKey) {
        this.format = format;
        this.key = key;
        this.scriptKey = scriptKey;
    }

    public FormField(String format, String scriptKey) {
        this.format = format;
        this.scriptKey = scriptKey;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getScriptKey() {
        return scriptKey;
    }

    public void setScriptKey(String scriptKey) {
        this.scriptKey = scriptKey;
    }
}
