import java.nio.charset.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;


import java.nio.ByteBuffer;

public class Message {

    Charset charset = StandardCharsets.UTF_16;
    static byte[] msgLength = new byte[4];
    static byte[] msgType = new byte[1];
    public static byte handshakeheader[] = "P2PFILESHARINGPROJ".getBytes();
    public static byte handshakezbits[] = "0000000000".getBytes();
    ArrayList<byte[]> fields = new ArrayList<byte[]>();
    public Message() {}

    public static ArrayList<byte[]> parseMessage (byte[] bArray)
    {
        ArrayList<byte[]> ListReturn = new ArrayList<byte[]>();
        ListReturn.add(Arrays.copyOfRange(bArray, 0, 4));
        ListReturn.add(Arrays.copyOfRange(bArray, 4, 5));
        ListReturn.add(Arrays.copyOfRange(bArray, 5, bArray.length));
        return ListReturn;
    }

    // Create header message from peer ID
    byte[] createHandshake(int ID) {

        // Create byte arrays for header, padding bits, and peer ID     
        byte handshakepid[] = String.format("%04d", ID).getBytes();

        // Return final byte array
        return Common.concat(handshakeheader, handshakezbits, handshakepid);
    }

    // Create 'choke' message
    byte[] createChoke() {
        
        // Message length will be 1 and the type will be 0
        msgLength = ByteBuffer.allocate(4).putInt(1).array();
        msgType[0] = 0;

        // Return final byte array
        return Common.concat(msgLength, msgType);
    }

    // Create 'unchoke' message
    byte[] createUnchoke() {

        // Message length will be 1 and the type will be 1
        msgLength = ByteBuffer.allocate(4).putInt(1).array();
        msgType[0] = 1;

        // Return final byte array
        return Common.concat(msgLength, msgType);
    }

    // Create 'interested' message
    public static byte[] createInterested() {

        // Message length will be 1 and the type will be 2
        msgLength = ByteBuffer.allocate(4).putInt(1).array();
        msgType[0] = 2;

        // Return final byte array
        return Common.concat(msgLength, msgType);
    }

    // Create 'not interested' message
    public static byte[] createNotInterested() {

        // Message length will be 1 and the type will be 3
        msgLength = ByteBuffer.allocate(4).putInt(1).array();
        msgType[0] = 3;

        // Return final byte array
        return Common.concat(msgLength, msgType);
    }

    // Create 'have' message
    public static byte[] createHave(byte[] indexField) {

        // Message length will be 5 and the type will be 4
        msgLength = ByteBuffer.allocate(4).putInt(5).array();
        msgType[0] = 4;

        // Return final byte array
        return Common.concat(msgLength, msgType, indexField);
    }

    // Create 'bitfield' message
    public static byte[] createBitfield(BitSet bitfield) {

        // Message length will depend on bitfield length and the type will be 5
        msgLength = ByteBuffer.allocate(4).putInt(1 + bitfield.toByteArray().length).array();
        msgType[0] = 5;

        // Return final byte array
        return Common.concat(msgLength, msgType, bitfield.toByteArray());
    }

    // Create 'request' message
    public static byte[] createRequest(byte[] indexField) {

        // Message length will be 5 and the type will be 6
        msgLength = ByteBuffer.allocate(4).putInt(5).array();
        msgType[0] = 6;

        // Return final byte array
        return Common.concat(msgLength, msgType, indexField);
    }

    // Create 'piece' message
    public static byte[] createPiece(byte[] indexField) {

        // Message length will be 5 and the type will be 7
        msgLength = ByteBuffer.allocate(4).putInt(5).array();
        msgType[0] = 7;

        // Return final byte array
        return Common.concat(msgLength, msgType, indexField);
    }
}
