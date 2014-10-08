package com.automahome.canbus.types;

public class Comando {
    private String nomeDoComando;
    private byte posicaoDoComando;
    private byte valorDoComando;
    
    public Comando(String nomeDoComando, byte posicaoDoComando, byte valorDoComando) {
        super();
        this.nomeDoComando = nomeDoComando;
        this.posicaoDoComando = posicaoDoComando;
        this.valorDoComando = valorDoComando;
    }

    public String getNomeDoComando() {
        return nomeDoComando;
    }

    public byte getPosicaoDoComando() {
        return posicaoDoComando;
    }
    
    public byte getValorDoComando() {
        return valorDoComando;
    }
}
