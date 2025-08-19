package com.example.test2.Student2.category;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String colorHex;

    public Category(String name, String colorHex) {
        this.name = name;
        this.colorHex = normalizeHex(colorHex);
    }

    private static String normalizeHex(String input) {
        final String DEFAULT = "#EEEEEE";

        if (input == null) return DEFAULT;

        String s = input.trim().toUpperCase();
        if (s.isEmpty()) return DEFAULT;

        // skini # da bismo čistili i seckali
        if (s.startsWith("#")) s = s.substring(1);

        // zadrži samo heks znakove
        s = s.replaceAll("[^0-9A-F]", "");
        if (s.isEmpty()) return DEFAULT;

        // Preferiraj 8 heks cifara ako ih ima dovoljno, inače 6
        if (s.length() >= 8) {
            s = s.substring(0, 8); // AARRGGBB
        } else if (s.length() >= 6) {
            s = s.substring(0, 6); // RRGGBB
        } else {
            // premalo: dopuni nulama do 6 cifara (RRGGBB)
            while (s.length() < 6) s += "0";
        }

        return "#" + s;
    }
}





