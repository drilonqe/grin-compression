package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The driver for the Grin compression program.
 */
public class Grin {
    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * 
     * @param infile  the file to decode
     * @param outfile the file to ouptut to
     * @throws IOException
     */
    public static void decode(String infile, String outfile) throws IOException {
        int magicNum = 0x736;
        BitInputStream input = new BitInputStream(infile);
        BitOutputStream output = new BitOutputStream(outfile);
        int header = input.readBits(32);
        if (header != magicNum) {
            input.close();
            output.close();
            throw new IllegalArgumentException();
        }

        HuffmanTree hTree = new HuffmanTree(input);
        hTree.decode(input, output);
        output.close();
        input.close();

    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of
     * those sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     * 
     * @param file the file to read
     * @return a freqency map for the given file
     * @throws IOException
     */
    public static Map<Short, Integer> createFrequencyMap(String file) throws IOException {
        Map<Short, Integer> frequencyMap = new HashMap<>();
        BitInputStream input = new BitInputStream(file);
        int bitsRead = input.readBits(8); // consume 8 bits at a time
        while (bitsRead != -1) {
            short asShort = (short) bitsRead;

            if (frequencyMap.containsKey(asShort)) {
                int count = frequencyMap.get(asShort);
                frequencyMap.put(asShort, count + 1); // count is incremented if there is multiple instances
            } else {
                frequencyMap.put(asShort, 1); // count is 1 if the map does not already contain the key
            }
            bitsRead = input.readBits(8); // read next
        }
        input.close();
        return frequencyMap;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * 
     * @param infile  the file to encode.
     * @param outfile the file to write the output to.
     */
    public static void encode(String infile, String outfile) {
        // TODO: fill me in!
    }

    /**
     * The entry point to the program.
     * 
     * @param args the command-line arguments.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java Grin <encode|decode> <infile> <outfile>");
            return;
        }
        String action = args[0];
        String infile = args[1];
        String outfile = args[2];

        if (action.equals("decode")) {
            decode(infile, outfile);
        } else {
            encode(infile, outfile);
        }
    }
}
