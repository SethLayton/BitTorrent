import java.nio.charset.*;
import java.nio.ByteBuffer;

public class Message {

    Charset charset = StandardCharsets.UTF_16;
    byte[] msgLength = new byte[4];
    byte[] msgType = new byte[1];
    
    public Message() {}

    // Create header message from peer ID
    byte[] createHandshake(int ID) {

        // Create byte arrays for header, padding bits, and peer ID
        byte handshakeheader[] = "P2PFILESHARINGPROJ".getBytes();
        byte handshakezbits[] = "0000000000".getBytes();
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
    byte[] createInterested() {

        // Message length will be 1 and the type will be 2
        msgLength = ByteBuffer.allocate(4).putInt(1).array();
        msgType[0] = 2;

        // Return final byte array
        return Common.concat(msgLength, msgType);
    }

    // Create 'not interested' message
    byte[] createNotInterested() {

        // Message length will be 1 and the type will be 3
        msgLength = ByteBuffer.allocate(4).putInt(1).array();
        msgType[0] = 3;

        // Return final byte array
        return Common.concat(msgLength, msgType);
    }

    // Create 'have' message
    byte[] createHave(byte[] indexField) {

        // Message length will be 5 and the type will be 4
        msgLength = ByteBuffer.allocate(4).putInt(5).array();
        msgType[0] = 4;

        // Return final byte array
        return Common.concat(msgLength, msgType, indexField);
    }

    // Create 'bitfield' message
    byte[] createBitfield(byte[] bitfield) {

        // Message length will depend on bitfield length and the type will be 5
        msgLength = ByteBuffer.allocate(4).putInt(1 + bitfield.length).array();
        msgType[0] = 5;

        // Return final byte array
        return Common.concat(msgLength, msgType, bitfield);
    }

    // Create 'request' message
    byte[] createRequest(byte[] indexField) {

        // Message length will be 5 and the type will be 6
        msgLength = ByteBuffer.allocate(4).putInt(5).array();
        msgType[0] = 6;

        // Return final byte array
        return Common.concat(msgLength, msgType, indexField);
    }

    // Create 'piece' message
    byte[] createPiece(byte[] indexField) {

        // Message length will be 5 and the type will be 7
        msgLength = ByteBuffer.allocate(4).putInt(5).array();
        msgType[0] = 7;

        // Return final byte array
        return Common.concat(msgLength, msgType, indexField);
    }
}
