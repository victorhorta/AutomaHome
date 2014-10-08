package com.automahome.canbus.types;

import java.util.List;

import com.automahome.canbus.mvc.CanNode;

public abstract class NodeType {
    protected byte typeCode;
    protected int iconResource; //com.automahome.canbus.R.drawable....
    protected String typeName;
    protected List<Comando> comandos;
    
    public String getTypeName() {
        return this.typeName;
    }
    
    public int getIconResource() {
        return this.iconResource;
    }
    
    public List<Comando> getComandos() {
        return this.comandos;
    }
    
    /**
     * Define a acao a ser tomada pelo tipo de No, para um conjunto especifico
     * de status bytes.
     * 
     * @param canNode
     *            No de referencia
     */
    public abstract void refreshStatus(CanNode canNode);
    
}
