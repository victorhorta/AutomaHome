package com.automahome.canbus.mvc;

import java.util.Arrays;
import java.util.List;

import com.automahome.canbus.Values;
import com.automahome.canbus.types.Comando;
import com.automahome.canbus.types.NodeType;

public class CanNode {
    private NodeType myType;
    private byte myId;
    private byte[] status;
    private String statusText;
    
    List<Comando> comandos; 
    
    CanNode(byte myId, byte myType) {
        this.myId = myId;
        this.myType = Values.nodeTypeMap.get(myType);
        status = new byte[Values.NODE_QTD_STATUS];
        statusText = "Aguardando status";
        this.comandos = this.myType.getComandos();
    }
    
    public NodeType getMyType() {
        return myType;
    }

    public void setMyType(NodeType myType) {
        this.myType = myType;
    }

    public byte getMyId() {
        return myId;
    }

    public void setMyId(byte myId) {
        this.myId = myId;
    }

    public byte[] getStatus() {
        return status;
    }
    
    public void setStatus(byte[] status) {
        this.status = Arrays.copyOf(status, status.length);
    }
        
    public String getStatusText() {
        return this.statusText;
    }
    
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void refreshStatus(byte[] status) {
        this.status = Arrays.copyOf(status, status.length);
        
    }
}
