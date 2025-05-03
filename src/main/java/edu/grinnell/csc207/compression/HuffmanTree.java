package edu.grinnell.csc207.compression;

import java.util.Map;
import java.util.PriorityQueue;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally
 * take 8 bits. However, we also need to encode a special EOF character to
 * denote the end of a .grin file. Thus, we need 9 bits to store each
 * byte value. This is fine for file writing (modulo the need to write in
 * byte chunks to the file), but Java does not have a 9-bit data type.
 * Instead, we use the next larger primitive integral type, short, to store
 * our byte values.
 */
public class HuffmanTree {
    private Node root;

    // Node class representing leaf nodes, and internal nodes
    public static class Node implements Comparable<Node> {
        int freq;
        short value;
        Node left;
        Node right;

        // Constructor for leafs
        Node(short value, int freq) {
            this.value = value;
            this.freq = freq;
            this.left = null;
            this.right = null;
        }

        // Constructor for internal nodes
        Node(Node left, Node right) {
            this.left = left;
            this.right = right;
            this.freq = left.freq + right.freq;
        }

        // check if node is a leaf
        boolean CheckLeaf() {
            return left == null && right == null;
        }

        @Override
        /**
         * Compare nodes based on frequency
         * 
         * @return -1 if this node's frequency is less than the other,
         *         1 if this node's frequency is more than other, and
         *         0 if frequencies are the same
         * 
         */
        public int compareTo(Node o) {
            if (freq < o.freq) {
                return -1;
            } else if (freq > o.freq) {
                return 1;
            } else {
                return 0; // same frequency
            }
        }

    }

    // Citation: Iterating through a Map using entrySet()
    // Source: https://www.geeksforgeeks.org/iterate-map-java/
    // https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html
    /**
     * Constructs a new HuffmanTree from a frequency map.
     * 
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree(Map<Short, Integer> freqs) {
        short eof = 256;
        freqs.put(eof, 1); // end of file only appears once

        PriorityQueue<Node> pQueue = new PriorityQueue<>();

        // source cited above
        for (Map.Entry<Short, Integer> entry : freqs.entrySet()) {
            short ch = entry.getKey(); // get character
            int f = entry.getValue(); // get frequency
            pQueue.add(new Node(ch, f));
        }

        // https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/util/PriorityQueue.html#poll()
        while (pQueue.size() != 1) { // go until we have one element
            Node sm1 = pQueue.poll(); // get smallest frequency node
            Node sm2 = pQueue.poll(); // get second smallest frequency node
            Node joined = new Node(sm1, sm2); // make internal node
            pQueue.add(joined); // put back in queue
        }

        root = pQueue.poll(); // whatever node remains should be the root
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     * 
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree(BitInputStream in) {
        this.root = loadTree(in);
    }

    public Node loadTree(BitInputStream in) {
        int nodeBit = in.readBit();
        if (nodeBit == -1) {
            throw new IllegalStateException(); // if stream is empty
        }
        if (nodeBit == 0) { // node is leaf
            int bitVal = in.readBits(9);
            if (bitVal == -1) {
                throw new IllegalStateException();
            }
            // freq can be set to anything
            return new Node((short) bitVal, 0); // leaf
        } else {
            Node left = loadTree(in);
            Node right = loadTree(in);
            return new Node(left, right); // internal node
        }
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * 
     * @param out the output file as a BitOutputStream
     */
    public void serialize(BitOutputStream out) {
        serializeH(root, out);
    }

    public void serializeH(Node cur, BitOutputStream out) {
        if (cur.CheckLeaf()) {
            out.writeBit(0); // if leaf write 0
            out.writeBits(cur.value, 9);
        } else {
            out.writeBit(1); // if internal node write 1
            serializeH(cur.left, out);
            serializeH(cur.right, out);
        }
    }

    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * 
     * @param in  the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode(BitInputStream in, BitOutputStream out) {
        // TODO: fill me in!
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of
     * bits into their uncompressed form, saving the results to the given
     * output stream. Note that the EOF character is not written to out
     * because it is not a valid 8-bit chunk (it is 9 bits).
     * 
     * @param in  the file to decompress.
     * @param out the file to write the decompressed output to.
     * 
     *            // classmate fui gave me advice for this function
     */
    public void decode(BitInputStream in, BitOutputStream out) {
        Node cur = root; // start from the root
        boolean isRunning = true;
        while (isRunning) {
            int bit = in.readBit();
            if (bit == -1) {
                throw new IllegalStateException(); // stream is empty
            }
            if (bit == 0) {
                cur = cur.left; // move to the left
            } else {
                cur = cur.right; // move to the right when = 1
            }

            if (cur.CheckLeaf()) {
                short value = cur.value;

                // stop if we have reached end of file
                if (value == 256) {
                    break;
                }
                // write 8 bits of characters back again
                out.writeBits(value, 8);
                // back to root for next
                cur = root;
            }

        }
    }
}