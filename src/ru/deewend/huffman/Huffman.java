package ru.deewend.huffman;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Huffman {
    public static final String OUTPUT_FILENAME = "encoded.dat";

    public static void main(final String[] args) {
        System.out.println("Huffman Coding Tool by deewend (Ivan Shubin) v1.0");

        if (args.length == 0) {
            System.out.println("Please specify whether you want to decode or encode text:");
            System.out.println("java -jar Huffman.jar decode");
            System.out.println("OR");
            System.out.println("java -jar Huffman.jar encode");

            return;
        }
        final boolean decode;
        final String mode = args[0];
        if (mode.equals("decode")) {
            decode = true;
        } else if (mode.equals("encode")) {
            decode = false;
        } else {
            System.out.println("Unknown mode: " + mode);

            return;
        }
        new Huffman().run(decode);
    }

    public void run(final boolean decode) {
        try {
            if (decode) {
                System.out.println("Decoded text: " + decode());

                return;
            }
            final String text;
            System.out.print("Enter the text you would like to encode: ");
            try (final Scanner input = new Scanner(System.in)) {
                text = input.nextLine();
            }
            if (text.isEmpty()) {
                System.out.println("Got an empty string");

                return;
            }
            encode(text);
            System.out.println("Done");
        } catch (final Throwable t) {
            System.out.println("A fatal error occurred while doing the job:");
            t.printStackTrace();
            if (!decode && (t instanceof StackOverflowError || t instanceof OutOfMemoryError)) {
                System.out.println("[Note] Looks like you requested a really large text to be encoded");
                System.out.println("[Note] To fix this error, try to increase " +
                        "stack size and memory limits by using -Xss and -Xmx options");
            }
            System.out.println("Sorry");
        }
    }

    private void encode(final String str) throws IOException {
        Objects.requireNonNull(str);

        if (str.isEmpty()) {
            throw new IllegalArgumentException();
        }
        final Map<Character, Long> occurrences;
        {
            occurrences = new HashMap<>();
            for (int i = 0; i < str.length(); i++) {
                final char character = str.charAt(i);
                if (occurrences.containsKey(character)) {
                    occurrences.put(character, occurrences.get(character) + 1L);
                } else {
                    occurrences.put(character, 1L);
                }
            }
        }
        final List<HuffmanNode> nodes = new ArrayList<>();
        for (final char key : occurrences.keySet()) {
            nodes.add(new HuffmanNode(occurrences.get(key), key));
        }
        while (nodes.size() != 1) {
            Collections.sort(nodes);
            final HuffmanNode first = nodes.get(0);
            final HuffmanNode second = nodes.get(1);
            nodes.remove(0);
            nodes.remove(0);
            nodes.add(new HuffmanNode(first, second));
        }

        String result = str;
        final Map<String, Character> dictionary = walk(nodes.get(0));
        try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME), StandardCharsets.UTF_8))) {
            writer.write(String.valueOf(dictionary.size()));
            writer.newLine();
            for (final String code : dictionary.keySet()) {
                final char character = dictionary.get(code);
                writer.write(code + "=" + character);
                writer.newLine();

                result = result.replace(String.valueOf(character), code);
            }
            writer.write(result);
            writer.newLine();
        }
    }

    private Map<String, Character> walk(final HuffmanNode node) {
        Objects.requireNonNull(node);

        final Map<String, Character> dictionary = new HashMap<>();
        walk(node, new StringBuilder(), dictionary);

        return dictionary;
    }

    private void walk(
            final HuffmanNode node,
            final StringBuilder stack,
            final Map<String, Character> dictionary
    ) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(dictionary);

        final HuffmanNode leftNode = node.getLeft();
        final HuffmanNode rightNode = node.getRight();
        if (leftNode != null) {
            walk(leftNode, stack.append('0'), dictionary);
            stack.setLength(stack.length() - 1);
            walk(rightNode, stack.append('1'), dictionary);
            stack.setLength(stack.length() - 1);
        } else {
            dictionary.put(stack.toString(), node.getCharacter());
        }
    }

    private String decode() throws IOException {
        final String encoded;
        final Map<String, Character> dictionary;
        final StringBuilder result = new StringBuilder();
        try (final Scanner input = new Scanner(new FileInputStream(OUTPUT_FILENAME), "UTF-8")) {
            {
                int numberOfEntriesInDictionary = input.nextInt();
                dictionary = new HashMap<>(numberOfEntriesInDictionary);
                input.nextLine();
                while (numberOfEntriesInDictionary-- > 0) {
                    final String[] parts = input.nextLine().split("=");
                    if (parts.length != 2) {
                        throw new CorruptedDataException();
                    }
                    final String code = parts[0];
                    ensureBinaryString(code);
                    final String character = parts[1];
                    if (character.length() != 1) {
                        throw new CorruptedDataException();
                    }
                    final char converted = character.charAt(0);

                    if (dictionary.containsKey(code)) {
                        throw new CorruptedDataException();
                    }
                    for (final String key : dictionary.keySet()) {
                        if (key.startsWith(code) || code.startsWith(key)) {
                            throw new CorruptedDataException();
                        }
                        if (dictionary.get(key) == converted) {
                            throw new CorruptedDataException();
                        }
                    }
                    dictionary.put(code, character.charAt(0));
                }
                encoded = input.nextLine();
                ensureBinaryString(encoded);
            }
            {
                int start = 0;
                for (int i = start + 1; i <= encoded.length(); i++) {
                    final String code = encoded.substring(start, i);
                    if (dictionary.containsKey(code)) {
                        start = i;
                        result.append(dictionary.get(code));
                    }
                }
                if (start != encoded.length()) {
                    throw new CorruptedDataException();
                }
            }
        }

        return result.toString();
    }

    private static void ensureBinaryString(final String str) {
        Objects.requireNonNull(str);

        for (int i = 0; i < str.length(); i++) {
            final char current = str.charAt(i);
            if (current != '0' && current != '1') {
                throw new CorruptedDataException();
            }
        }
    }
}
