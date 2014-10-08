package com.automahome.canbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.automahome.canbus.mvc.CanNode;
import com.automahome.canbus.types.NodeType;
import com.automahome.canbus.types.NodeTypeClima;
import com.automahome.canbus.types.NodeTypeLampada;

public class Values {
    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE= 2;
    public static final int REQUEST_ENABLE_BT = 3;
    
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_TYPE_SELECTED = "device_type_selected";
    
    // No devices
    public static final String NONE_PAIRED = "None paired";
    
    
    // Intents Activity -> Service
    public static final String INTENT_COMMAND_REQUEST = "command_request";
    public static final String EXTRA_COMMAND_REQUEST_COMMAND = "command_request_command";
    
    // BTService confirming that command has been properly sent (sendDataCR raised exception or not)
    public static final String INTENT_COMMAND_SUCCESS = "command_success";
    public static final String INTENT_COMMAND_ERROR = "command_error";
    
    // Grammar values and Strings
    public static final String TOKEN_TEST = "CELULAR";
    public static final String TOKEN_REQUPDATE = "REQUPD";
    
    
    // Grammar keys
    public static final byte TYPE_LAMP = 0x01;
    public static final byte TYPE_IR = 0x02;
    public static final byte TYPE_CLIMA = 0x03;
    public static final byte INSTRUCT_UPDATE = 0x01;
    public static final byte INSTRUCT_REQ_UPDATE = 0x02; 
    public static final byte INSTRUCT_SET = 0x03;
    public static final byte END_CHAR = 13;
    public static final byte NODE_QTD_STATUS = 3;
    
    //public static Map<Byte, CanNode> nodeMap = new HashMap<Byte, CanNode>();
    
    public static List<CanNode> canNodes = new ArrayList<CanNode>();
    public static final Map<Byte, NodeType> nodeTypeMap = new HashMap<Byte, NodeType>();
    
    static {
        nodeTypeMap.put(Values.TYPE_LAMP, new NodeTypeLampada());
        nodeTypeMap.put(Values.TYPE_CLIMA, new NodeTypeClima());
    }
    
    
}
