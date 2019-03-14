package test;

import java.io.*;

final class FileWriter {
    private static final byte[] letter = new byte[]{77,105,110,105,45,80,78,71,72,0,0,0,9,0,0,0,8,0,0,0,10,0,67,0,0,0,11,76,97,32,108,101,116,116,114,101,32,74,68,0,0,0,10,0,0,-25,-25,-25,-25,-25,-25,-121,-121};;

    public static void main(String[] args)
    {
        try{
        DataOutputStream dos =
                new DataOutputStream(new BufferedOutputStream
                        (new FileOutputStream("OutputFile.txt")));
        dos.write(letter);
        dos.close();
        } catch (FileNotFoundException e){
            System.out.println("File not found : " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
