package ru.deewend.huffman;

import java.util.Objects;

@SuppressWarnings("CommentedOutCode")
public final class HuffmanNode implements Comparable<HuffmanNode> {
    private final long weight;
    private final Character character;
    private final HuffmanNode left, right;

    public HuffmanNode(final long weight, final char character) {
        if (weight < 1L) {
            throw new IllegalArgumentException();
        }
        this.weight = weight;
        this.character = character;
        this.left = null;
        this.right = null;
    }

    public HuffmanNode(final HuffmanNode left, final HuffmanNode right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);

        this.weight = left.weight + right.weight;
        this.character = null;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(final HuffmanNode o) {
        return Long.compare(weight, o.weight);
    }

    public char getCharacter() {
        if (character == null) {
            throw new IllegalStateException();
        }

        return character;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    /*
    // for debug purposes
    @Override
    public String toString() {
        return "{ left: " + left + ", right: " + right + " }";
    }
     */
}
