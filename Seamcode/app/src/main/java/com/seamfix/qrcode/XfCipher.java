//package com.seamfix.qrcode;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.UUID;
//
///**
// * Encrypts string
// */
//
//public class XfCipher {
//    private static final int CONTENT_LEN = 2000;
//    private static final int TOTAL_LEN = CONTENT_LEN + 256;
//
//    private static final int SCRAMBLING_ALGO = 0;
//    private static final int SALT_LENGTH     = CONTENT_LEN + 251;
//    private static final int FLOW_LENGTH     = CONTENT_LEN + 252;
//    private static final int SALT_INDEX      = CONTENT_LEN + 253;
//    private static final int FLOW_INDEX      = CONTENT_LEN + 254;
//    private static final int PATTERN         = CONTENT_LEN + 255;
//
//
//    public static void main(String []args){
//        ArrayList<String> data =  new ArrayList<>();
//        data.add("EMMANUEL");
//        data.add("EDWARDS");
//        data.add("IE12345678");
//        data.add("321");
//        String scrambleData = scramble(data);
//        String dataPlain = unScramble(scrambleData);
//        System.out.println(dataPlain);
//    }
//
//    /**
//     * Scrambles a plain text using seamfix-cipher
//     * @param dataInput plain data text
//     * @return scrambled string
//     */
//    public static String scramble(ArrayList<String> dataInput) {
//
//        /*
//         Swap concatenation using concatenation processor
//         ....................................................
//         TODO
//         */
//        String rawData = String.join(":", dataInput);
//        String checkSum = getChecksum(rawData);
//
//        ConcatenationResult concatenationResult = new Concatenator().concatenate(dataInput);
//        String data = concatenationResult.getOutput();
//        ConcatPattern concatPattern = concatenationResult.getConcatPattern();
//
//        byte[]salt     = checkSum.getBytes();
//        byte[]flow     = data.getBytes();
//        int flowLength = flow.length;
//        int saltLength = salt.length;
//        int pattern    = concatPattern.getConcatPatternKey();
//
//        /*
//        Scramble plain text
//        .....................................................
//         */
//        for (byte c : salt) {
//            for (int j = 0; j < flowLength; j++) {
//                int r = flow[j] + c;
//                flow[j] = (byte)r;
//            }
//        }
//
//        /*
//        Embed checksum into cipher for salt
//        ..................................................
//         */
//        int saltIndex = 0;
//        byte[] combo = new byte[TOTAL_LEN];
//        for (int n = 1; n <= salt.length; n++) {
//            int p = 1 + (n - 1) * 2;
//            combo[p] = salt[n-1];
//            saltIndex = p;
//        }
//
//        int flowIndex = 0;
//        for (int n = 1; n <= flow.length; n++) {
//            int p = 2 + (n - 1) * 2;
//            combo[p] = flow[n-1];
//            flowIndex = p;
//        }
//
//        /*
//        generate padding for the rest of the empty address
//        padding begins from the longest between flow or salt
//        .....................................................
//         */
//        int boundary   = Math.max(flowIndex, saltIndex);
//        int padStart   = boundary + 1;
//        int space      = TOTAL_LEN - (padStart);
//        String padding = "";
//        while (padding.length() < space){
//            padding = padding.concat(UUID.randomUUID().toString());
//        }
//        padding    = padding.substring(0,space);
//        byte[]pads = padding.getBytes();
//
//        /*
//        perform scrambling on padding
//        .............................
//         */
//        for (byte c : salt) {
//            for (int j = 0; j < pads.length; j++) {
//                int r = pads[j] + c;
//                pads[j] = (byte)r;
//            }
//        }
//
//        /*
//        Apply scrambled padding to final scrambled text
//        ......................................................
//         */
//        System.arraycopy(pads, 0, combo, padStart, space);
//
//        /*
//        Add salt length index to position
//        .......................................................
//         */
//        combo[SALT_LENGTH] = (byte)saltLength;
//
//        /*
//        Add flow length index to position
//        .......................................................
//         */
//        combo[FLOW_LENGTH] = (byte)flowLength;
//
//        /*
//        Add scrambled salt length index to end of scrambled text
//        .......................................................
//         */
//        combo[SALT_INDEX] = (byte)saltIndex;
//
//        /*
//        Add scrambled flow length index to end of scrambled text
//        .......................................................
//         */
//        combo[FLOW_INDEX] = (byte)flowIndex;
//
//        /*
//        Add scrambling type to end of scrambled text
//        .......................................................
//         */
//        combo[SCRAMBLING_ALGO] = (byte)'a';
//
//        /*
//        Add concatenation pattern type to end of scrambled text
//        .......................................................
//         */
//        combo[PATTERN] = (byte)(48 + pattern);
//
//        /*
//        Print out and return ciphered text
//        .......................................................
//         */
//        System.out.println(combo.length);
//        System.out.println(new String(flow));
//        System.out.println(new String(combo));
//        return Base64.getEncoder().encodeToString(combo);
//    }
//
//    /**
//     * Unscrambles a seamfix-cipher-201 scrambled data
//     *
//     * @param scrambled the scrambled string data
//     * @return a plain original text of the data
//     */
//    public static String unScramble(String scrambled){
//        if(scrambled == null){
//            return null;
//        }
//
//        /*
//        Detect metadata type from scrambled string
//        ...................................................
//         */
//        byte[] cipherData  = Base64.getDecoder().decode(scrambled);
//        char scType        = (char)cipherData[SCRAMBLING_ALGO];
//        int saltLength     = cipherData[SALT_LENGTH];
//        int flowLength     = cipherData[FLOW_LENGTH];
//        int saltIndex      = cipherData[SALT_INDEX];
//        int flowIndex      = cipherData[FLOW_INDEX];
//        byte pattern       = (byte)(cipherData[PATTERN] - 48);
//
//
//        /*
//        Detect scrambled data from scrambled string
//        ...................................................
//         */
//        int dataIndex    = Math.max(flowIndex, saltIndex);
//        int dataLength   = dataIndex + 1;
//        byte[] text      = new byte[dataLength];
//        System.arraycopy(cipherData, 0, text, 0, dataLength);
//
//        /*
//        Get salt from text
//        ....................................................
//         */
//        byte[] salt = new byte[saltLength];
//        for (int n = 1; n <= saltLength; n++) {
//            int p = 1 + (n - 1) * 2;
//            salt[n-1] = text[p];
//        }
//        System.out.println(new String(salt));
//
//        /*
//        Get get scrambled data from text
//        ....................................................
//         */
//        byte[] data = new byte[flowLength];
//        for (int n = 1; n <= flowLength; n++) {
//            int p = 2 + (n - 1) * 2;
//            data[n-1] = text[p];
//        }
//        System.out.println(new String(data));
//
//        /*
//        Get get unscramble data from text
//        ....................................................
//         */
//        for(byte c : salt){
//            for (int j = 0; j <data.length; j++) {
//                int r = data[j] - c;
//                data[j] = (byte) r;
//            }
//        }
//
//        String[] dataArray = new String(data).split(":");
//        ArrayList<String> al = new ArrayList<>(Arrays.asList(dataArray));
//        String out = new Concatenator().reArrangeList(al, pattern);
//
//        System.out.println((out));
//        if(new String(salt).equals(getChecksum(out))){
//            System.out.println((true));
//        }
//        return out;
//    }
//
//    /**
//     * Gets the checksum of string data
//     * @param data data;
//     * @return checksum
//     */
//    private static String getChecksum(String data) {
//        if (data == null) {
//            return null;
//        }
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] fileData = data.getBytes();
//            md.update(fileData);
//            byte[] digest = md.digest();
//            char[] checksum = Hex.encodeHex(digest);
//            return (new String(checksum));
//        } catch (NoSuchAlgorithmException var5) {
//            var5.printStackTrace();
//            return null;
//        }
//    }
//
//
//}
//
//
