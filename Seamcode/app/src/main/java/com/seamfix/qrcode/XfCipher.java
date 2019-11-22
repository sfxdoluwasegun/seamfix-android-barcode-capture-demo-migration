package com.seamfix.qrcode;

import com.google.gson.Gson;
import com.sf.bio.lib.Hex;
import com.sf.bio.lib.util.Crypter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encrypts string
 */

public class XfCipher {
    private static int CONTENT_LEN = 2000;
    private static int TOTAL_LEN = CONTENT_LEN + 256;

    private static int SCRAMBLING_ALGO = 0;
    private static int SALT_LENGTH     = CONTENT_LEN + 251;
    private static int FLOW_LENGTH     = CONTENT_LEN + 252;
    private static int SALT_INDEX      = CONTENT_LEN + 253;
    private static int FLOW_INDEX      = CONTENT_LEN + 254;
    private static int PATTERN         = CONTENT_LEN + 255;


    public static void main(String []args){

        String data = "{\"d\":\"RIGHT_THUMB\",\"f\":\"[-11.557151,1.4869788,11.731017,-9.2512455,3.605088,0.57191235,-2.1170497,-0.0019485046,1.755438,-1.0786216,-0.9833641,-0.21956089,0.84275484,3.7611775,-1.25726]\",\"p\":\"Rk1SADAzMAAAAAEkAQMAAAAAAAEAAAAd/gEDAAEBkAH0AMUAxSqAWACUnP6AmwAua/5ApwA+ef5AhwEWU/5AoQEqXP6A2QDqXP6A0QFCYv5A2AGkY/5BSAF3av6AkwCsQP6AqgCGOP6AvgCeQP5BBACCEv6BKACib/6BHgE2Z/5BNgGOEv6AbQA7s/5BPQDbbf5AVQD2p/6AfgBUmv6AkwBKe/5AnACYO/5AsgCMnv5AlwEyBv5A1gB0Mv5A0gDMWv6A2wCyR/6BEAF4af6BRAFQD/5AlwG9Yv5BZgC3Gf5AZgFCUP6AyACOsP6ApQFcYv6A1gCCQ/6A2ACmWf5AqQGcCf5AxwGAZP5BJgDKa/6BLQDMF/6BVACmcv6A3ABxHv4AAA\\u003d\\u003d\",\"t\":\"Bdbxhshs:Bdbfbhf:HDHDHDHD:659\"}";

        byte[]encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", data);
        char[] encHex = Hex.encodeHex(encData);
        EnrollmentData df  = new Gson().fromJson(data, EnrollmentData.class);
        EnrollmentData2 data2 = new EnrollmentData2();

        data2.setT(df.getT());
        data2.setD(df.getD());
        data2.setP(java.util.Base64.getDecoder().decode(df.getP()));

        float[] ff = new Gson().fromJson(df.getF(), float[].class);
        //data2.setF(ff);


        String ggg = Arrays.toString(ff);
        data2.setG(java.util.Base64.getEncoder().encodeToString(ggg.getBytes()));

        String main  = new Gson().toJson(data2);
        byte[]encData2 = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", main);


        String encString = FingerQrCode.encode(df);//getRawStringencData); //Base64.encodeToString(encData, Base64.NO_WRAP);
        EnrollmentData enrollmentData = FingerQrCode.decode(encString);

        //String encString = new String(encHex);//getRawString(encData); //Base64.encodeToString(encData, Base64.NO_WRAP);
        //String encString = new String(encData, StandardCharsets.UTF_8);
        byte[] dd = encString.getBytes(StandardCharsets.UTF_8);

        String mm = Crypter.decrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", dd);
        System.out.println(mm);



//        //new XfCipher().scrambled(data);
//        EnrollmentData df  = new Gson().fromJson(data, EnrollmentData.class);
//
//
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutput out = null;
//        try {
//            out = new ObjectOutputStream(bos);
//            out.writeObject(df);
//            out.flush();
//            byte[] yourBytes = bos.toByteArray();
//            System.out.println(yourBytes.length);
//            String dataString  =Base64.getEncoder().encodeToString(yourBytes);
//            System.out.println(dataString);
//            System.out.print(dataString.length());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                bos.close();
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//        }

        //String scrambleData = scramble(data);
        //String dataPlain = unScramble(scrambleData);
        //System.out.println(dataPlain);
    }


    private String scrambled(String dataInput){
        dataInput = dataInput.toUpperCase();
        String checkSum = "w"; //getChecksum(dataInput);//e2hf223365521aafttp
        if(checkSum == null){
            return  null;
        }
        char[] data     = dataInput.toCharArray();
        char[] salt     = checkSum.toCharArray();
        int dataLength  = data.length;
        int saltLength  = salt.length;

        /*
        Scramble plain text
        .....................................................
         */
        for (char c : salt) {
            for (int j = 0; j < dataLength; j++) {
                int n = data[j];
                int d = (c % 0x0A);
                int r = n + d;
                int m = head(r);
                data[j] = (char) m;
            }
        }
        System.out.println(data);

        /*
        Scramble plain text
        .....................................................
         */
        for (int k = 0; k< saltLength; k++ ) {
            for (int j = 0; j < dataLength; j++) {
                int n = data[j];
                int d = (salt[k] % 0x0A);
                int r = n - d;
                int m = head(r);
                data[j] = (char) m;
            }
        }
        System.out.println(data);
        EnrollmentData df  = new Gson().fromJson(new String(data), EnrollmentData.class);
        return null;
    }


    private int head(int val){
        return ((val >= 32) && (val <= 126))? val : rollOver(val);
    }


    private int rollOver(int val){
        return (val > 32)? 32 + (val - 126) : 126 - Math.abs(val);
    }

    /**
     * Scrambles a plain text using seamfix-cipher
     * @param dataInput plain data text
     * @return scrambled string
     */
    public static String scramble(String dataInput) {

         CONTENT_LEN = dataInput.length();
         TOTAL_LEN = CONTENT_LEN + 6;

         SCRAMBLING_ALGO = 0;
         SALT_LENGTH     = CONTENT_LEN + 1;
         FLOW_LENGTH     = CONTENT_LEN + 2;
         SALT_INDEX      = CONTENT_LEN + 3;
         FLOW_INDEX      = CONTENT_LEN + 4;
         PATTERN         = CONTENT_LEN + 5;

        /*
         Swap concatenation using concatenation processor
         ....................................................
         TODO
         */

        String checkSum = getChecksum(dataInput);
        byte[]salt      = checkSum.getBytes();
        byte[]flow      = dataInput.getBytes();
        int flowLength  = flow.length;
        int saltLength  = salt.length;

        /*
        Scramble plain text
        .....................................................
         */
        for (byte c : salt) {
            for (int j = 0; j < flowLength; j++) {
                int r = flow[j] + c;
                flow[j] = (byte)r;
            }
        }

        /*
        Embed checksum into cipher for salt
        ..................................................
         */
        int saltIndex = 0;
        byte[] combo = new byte[TOTAL_LEN];
        for (int n = 1; n <= salt.length; n++) {
            int p = 1 + (n - 1) * 2;
            combo[p] = salt[n-1];
            saltIndex = p;
        }

        int flowIndex = 0;
        for (int n = 1; n <= flow.length; n++) {
            int p = 2 + (n - 1) * 2;
            combo[p] = flow[n-1];
            flowIndex = p;
        }

        /*
        generate padding for the rest of the empty address
        padding begins from the longest between flow or salt
        .....................................................
         */
        int boundary   = Math.max(flowIndex, saltIndex);
        int padStart   = boundary + 1;
        int space      = TOTAL_LEN - (padStart);
        String padding = "";
        while (padding.length() < space){
            padding = padding.concat(UUID.randomUUID().toString());
        }
        padding    = padding.substring(0,space);
        byte[]pads = padding.getBytes();

        /*
        perform scrambling on padding
        .............................
         */
        for (byte c : salt) {
            for (int j = 0; j < pads.length; j++) {
                int r = pads[j] + c;
                pads[j] = (byte)r;
            }
        }

        /*
        Apply scrambled padding to final scrambled text
        ......................................................
         */
        System.arraycopy(pads, 0, combo, padStart, space);

        /*
        Add salt length index to position
        .......................................................
         */
        combo[SALT_LENGTH] = (byte)saltLength;

        /*
        Add flow length index to position
        .......................................................
         */
        combo[FLOW_LENGTH] = (byte)flowLength;

        /*
        Add scrambled salt length index to end of scrambled text
        .......................................................
         */
        combo[SALT_INDEX] = (byte)saltIndex;

        /*
        Add scrambled flow length index to end of scrambled text
        .......................................................
         */
        combo[FLOW_INDEX] = (byte)flowIndex;

        /*
        Add scrambling type to end of scrambled text
        .......................................................
         */
        combo[SCRAMBLING_ALGO] = (byte)'a';

        /*
        Add concatenation pattern type to end of scrambled text
        .......................................................
         */
        combo[PATTERN] = (byte)(48 + 1);

        /*
        Print out and return ciphered text
        .......................................................
         */
        System.out.println(combo.length);
        System.out.println(new String(flow));
        System.out.println(new String(combo));
        return null;
    }

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
////        String[] dataArray = new String(data).split(":");
////        ArrayList<String> al = new ArrayList<>(Arrays.asList(dataArray));
////        String out = new Concatenator().reArrangeList(al, pattern);
////
////        System.out.println((out));
////        if(new String(salt).equals(getChecksum(out))){
////            System.out.println((true));
////        }
//        return null;
//    }

    /**
     * Gets the checksum of string data
     * @param data data;
     * @return checksum
     */
    private static String getChecksum(String data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileData = data.getBytes();
            md.update(fileData);
            byte[] digest = md.digest();
            char[] checksum = Hex.encodeHex(digest);
            return (new String(checksum));
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
            return null;
        }
    }


    private static byte[] compress(byte[] encData) {
        try {
            //byte[] encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(encData);
            gzip.close();
            byte[] compressed = bos.toByteArray();
            bos.close();
            return compressed;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }

    private static String encode(EnrollmentData data){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.flush();
            byte[] encoded = bos.toByteArray();
            byte[] yourBytes = compress(encoded);
            byte[] encData = Crypter.encrypt("E5572DA36352063BBBA1321799B941B8FC337E64F5B4AB15EB01B9F68FB946A1", yourBytes);

            System.out.println(yourBytes.length);
            String dataString  = java.util.Base64.getEncoder().encodeToString(encData);
            System.out.println(dataString);
            System.out.print(dataString.length());
            return dataString;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }


}


