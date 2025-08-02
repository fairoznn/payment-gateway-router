package com.fairoz.model;

public class GatewayConfig {
    private String name;
    private Integer weight;
    private Boolean enabled;
    
    public GatewayConfig() {}
    
    public GatewayConfig(String name, Integer weight, Boolean enabled) {
        this.name = name;
        this.weight = weight;
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}