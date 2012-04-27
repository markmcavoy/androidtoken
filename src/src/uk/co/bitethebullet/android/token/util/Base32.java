package uk.co.bitethebullet.android.token.util;

import java.util.ArrayList;

public class Base32 {

	private static final String DEF_ENCODING_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final char DEF_PADDING = '=';

    private String eTable; //Encoding table
    private char padding;
    private byte[] dTable; //Decoding table

    public Base32 ()
    {
    	this (DEF_ENCODING_TABLE, DEF_PADDING);
    }
    
    public Base32 (char padding){
    	this (DEF_ENCODING_TABLE, padding);
    }
    public Base32 (String encodingTable){
    	this (encodingTable, DEF_PADDING);
    }

    public Base32 (String encodingTable, char padding) {
        this.eTable = encodingTable;
        this.padding = padding;
        dTable = new byte[0x80];
        InitialiseDecodingTable ();
    }

    public String encodeBytes (byte[] input) {
        StringBuffer output = new StringBuffer ();
        int specialLength = input.length % 5;
        int normalLength = input.length - specialLength;
        
        for (int i = 0; i < normalLength; i += 5) {
            int b1 = input[i] & 0xff;
            int b2 = input[i + 1] & 0xff;
            int b3 = input[i + 2] & 0xff;
            int b4 = input[i + 3] & 0xff;
            int b5 = input[i + 4] & 0xff;

            output.append(eTable.charAt((b1 >> 3) & 0x1f));
            output.append(eTable.charAt(((b1 << 2) | (b2 >> 6)) & 0x1f));
            output.append(eTable.charAt((b2 >> 1) & 0x1f));
            output.append(eTable.charAt(((b2 << 4) | (b3 >> 4)) & 0x1f));
            output.append(eTable.charAt(((b3 << 1) | (b4 >> 7)) & 0x1f));
            output.append(eTable.charAt((b4 >> 2) & 0x1f));
            output.append(eTable.charAt(((b4 << 3) | (b5 >> 5)) & 0x1f));
            output.append(eTable.charAt(b5 & 0x1f));
        }

        switch (specialLength) {
            case 1: {
                    int b1 = input[normalLength] & 0xff;
                    output.append (eTable.charAt((b1 >> 3) & 0x1f));
                    output.append (eTable.charAt((b1 << 2) & 0x1f));
                    output.append (padding).append (padding).append (padding).append (padding).append (padding).append (padding);
                    break;
                }

            case 2: {
                    int b1 = input[normalLength] & 0xff;
                    int b2 = input[normalLength + 1] & 0xff;
                    output.append (eTable.charAt((b1 >> 3) & 0x1f));
                    output.append (eTable.charAt(((b1 << 2) | (b2 >> 6)) & 0x1f));
                    output.append (eTable.charAt((b2 >> 1) & 0x1f));
                    output.append (eTable.charAt((b2 << 4) & 0x1f));
                    output.append (padding).append (padding).append (padding).append (padding);
                    break;
                }
            case 3: {
                    int b1 = input[normalLength] & 0xff;
                    int b2 = input[normalLength + 1] & 0xff;
                    int b3 = input[normalLength + 2] & 0xff;
                    output.append (eTable.charAt((b1 >> 3) & 0x1f));
                    output.append (eTable.charAt(((b1 << 2) | (b2 >> 6)) & 0x1f));
                    output.append (eTable.charAt((b2 >> 1) & 0x1f));
                    output.append (eTable.charAt(((b2 << 4) | (b3 >> 4)) & 0x1f));
                    output.append (eTable.charAt((b3 << 1) & 0x1f));
                    output.append (padding).append (padding).append (padding);
                    break;
                }
            case 4: {
                    int b1 = input[normalLength] & 0xff;
                    int b2 = input[normalLength + 1] & 0xff;
                    int b3 = input[normalLength + 2] & 0xff;
                    int b4 = input[normalLength + 3] & 0xff;
                    output.append (eTable.charAt((b1 >> 3) & 0x1f));
                    output.append (eTable.charAt(((b1 << 2) | (b2 >> 6)) & 0x1f));
                    output.append (eTable.charAt((b2 >> 1) & 0x1f));
                    output.append (eTable.charAt(((b2 << 4) | (b3 >> 4)) & 0x1f));
                    output.append (eTable.charAt(((b3 << 1) | (b4 >> 7)) & 0x1f));
                    output.append (eTable.charAt((b4 >> 2) & 0x1f));
                    output.append (eTable.charAt((b4 << 3) & 0x1));
                    output.append (padding);
                    break;
                }
        }

        return output.toString();
    }

    public byte[] decodeBytes (String data) {
		ArrayList<Byte> outStream = new ArrayList<Byte>();

        int length = data.length();
        
        while (length > 0) {
            if (!this.Ignore (data.charAt(length - 1))) break;
            length--;
        }

        int i = 0;
        int finish = length - 8;
        for (i = this.NextI (data, i, finish); i < finish; i = this.NextI (data, i, finish)) {
            byte b1 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b2 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b3 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b4 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b5 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b6 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b7 = dTable[data.charAt(i++)];
            i = this.NextI (data, i, finish);
            byte b8 = dTable[data.charAt(i++)];

            outStream.add ((byte)((b1 << 3) | (b2 >> 2)));
            outStream.add ((byte)((b2 << 6) | (b3 << 1) | (b4 >> 4)));
            outStream.add ((byte)((b4 << 4) | (b5 >> 1)));
            outStream.add ((byte)((b5 << 7) | (b6 << 2) | (b7 >> 3)));
            outStream.add ((byte)((b7 << 5) | b8));
        }
        this.DecodeLastBlock (outStream,
            data.charAt(length - 8), data.charAt(length - 7), data.charAt(length - 6), data.charAt(length - 5),
            data.charAt(length - 4), data.charAt(length - 3), data.charAt(length - 2), data.charAt(length - 1));
        
        byte[] result = new byte[outStream.size()];
        for(int j = 0; j < outStream.size(); j++){
        	result[j] = outStream.get(j).byteValue();
        }
        
        return result;
    }

    protected int DecodeLastBlock (ArrayList<Byte> outStream, char c1, char c2, char c3, char c4, char c5, char c6, char c7, char c8) {
        if (c3 == padding) {
            byte b1 = dTable[c1];
            byte b2 = dTable[c2];
            outStream.add ((byte)((b1 << 3) | (b2 >> 2)));
            return 1;
        }

        if (c5 == padding) {
            byte b1 = dTable[c1];
            byte b2 = dTable[c2];
            byte b3 = dTable[c3];
            byte b4 = dTable[c4];
            outStream.add ((byte)((b1 << 3) | (b2 >> 2)));
            outStream.add ((byte)((b2 << 6) | (b3 << 1) | (b4 >> 4)));
            return 2;
        }

        if (c6 == padding) {
            byte b1 = dTable[c1];
            byte b2 = dTable[c2];
            byte b3 = dTable[c3];
            byte b4 = dTable[c4];
            byte b5 = dTable[c5];

            outStream.add ((byte)((b1 << 3) | (b2 >> 2)));
            outStream.add ((byte)((b2 << 6) | (b3 << 1) | (b4 >> 4)));
            outStream.add ((byte)((b4 << 4) | (b5 >> 1)));
            return 3;
        }

        if (c8 == padding) {
            byte b1 = dTable[c1];
            byte b2 = dTable[c2];
            byte b3 = dTable[c3];
            byte b4 = dTable[c4];
            byte b5 = dTable[c5];
            byte b6 = dTable[c6];
            byte b7 = dTable[c7];

            outStream.add ((byte)((b1 << 3) | (b2 >> 2)));
            outStream.add ((byte)((b2 << 6) | (b3 << 1) | (b4 >> 4)));
            outStream.add ((byte)((b4 << 4) | (b5 >> 1)));
            outStream.add ((byte)((b5 << 7) | (b6 << 2) | (b7 >> 3)));
            return 4;
        }

        else {
            byte b1 = dTable[c1];
            byte b2 = dTable[c2];
            byte b3 = dTable[c3];
            byte b4 = dTable[c4];
            byte b5 = dTable[c5];
            byte b6 = dTable[c6];
            byte b7 = dTable[c7];
            byte b8 = dTable[c8];
            outStream.add ((byte)((b1 << 3) | (b2 >> 2)));
            outStream.add ((byte)((b2 << 6) | (b3 << 1) | (b4 >> 4)));
            outStream.add ((byte)((b4 << 4) | (b5 >> 1)));
            outStream.add ((byte)((b5 << 7) | (b6 << 2) | (b7 >> 3)));
            outStream.add ((byte)((b7 << 5) | b8));
            return 5;
        }
    }

    protected int NextI (String data, int i, int finish) {
        while ((i < finish) && this.Ignore (data.charAt(i))) i++;

        return i;
    }

    protected boolean Ignore (char c) {
        return (c == '\n') || (c == '\r') || (c == '\t') || (c == ' ') || (c == '-');
    }

    protected void InitialiseDecodingTable () {
        for (int i = 0; i < eTable.length(); i++) {
            dTable[eTable.charAt(i)] = (byte)i;
        }
    }
	
	
}
