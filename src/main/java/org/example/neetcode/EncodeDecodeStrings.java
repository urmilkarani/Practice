package org.example.neetcode;

import java.util.ArrayList;
import java.util.List;

public class EncodeDecodeStrings {
    public String encode(List<String> values) {
        StringBuilder encoded = new StringBuilder();
        for (String value : values) encoded.append(value.length()).append('#').append(value);
        return encoded.toString();
    }

    public List<String> decode(String encoded) {
        List<String> result = new ArrayList<>();
        for (int start = 0; start < encoded.length();) {
            int separator = start;
            while (encoded.charAt(separator) != '#') separator++;
            int length = Integer.parseInt(encoded.substring(start, separator));
            start = separator + 1;
            result.add(encoded.substring(start, start + length));
            start += length;
        }
        return result;
    }

    public static void main(String[] args) {
        EncodeDecodeStrings solver = new EncodeDecodeStrings();
        String encoded = solver.encode(List.of("neet", "code", "#"));
        System.out.println(solver.decode(encoded));
    }
}
