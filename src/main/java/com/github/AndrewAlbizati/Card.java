package com.github.AndrewAlbizati;

public class Card {
    private byte value;
    public Card() {}

    public Card(byte value) {
        this.value = value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return switch(value)  {
            case 11, 12, 13 -> 10;
            default -> value;
        };
    }
}
