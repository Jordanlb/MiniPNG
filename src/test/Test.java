package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Test {

    /*
    //Fonction pour escape caractères spéciaux dans le nom du fichier :

    private static final Pattern PATTERN = Pattern.compile("[^A-Za-z0-9_\\-]");

    private static final int MAX_LENGTH = 127;

    public static String escapeStringAsFilename(String in){

        StringBuffer sb = new StringBuffer();

        // Apply the regex.
        Matcher m = PATTERN.matcher(in);

        while (m.find()) {

            // Convert matched character to percent-encoded.
            String replacement = "%"+Integer.toHexString(m.group().charAt(0)).toUpperCase();

            m.appendReplacement(sb,replacement);
        }
        m.appendTail(sb);

        String encoded = sb.toString();

        // Truncate the string.
        int end = Math.min(encoded.length(),MAX_LENGTH);
        return encoded.substring(0,end);
    }
    */


    public static void main(String[] args){
        final byte[] data;
        if(args.length<1) {
            System.out.println("Mauvaise utilisation programme, utiliser de la forme : java -cp ./out/test.Test filename");
        }
        data = readBytesFromFile(args[0]);
        String[] temp = traiteData(data.clone());;
        afficheData(temp);
    }

    private static byte[] readBytesFromFile(String filename) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filename);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);

            if (fileInputStream.read(bytesArray) != -1)
                System.out.println("fichier lu");
            else {
                System.out.println("Pas d'octet dans le fichier");
                System.exit(0);
            }

        } catch (FileNotFoundException e) {
            System.err.println("erreur interne : fichier non trouvé");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }


    // Revoir utilisation des blocs, utilisation naive de A.mp, si les blocs sont dans l'ordre. Creer des listes de bloc C, D et H dans lesquels stocker ces blocs -> On accepte que Header et un bloc D
    private static String[] traiteData(byte[] data) {
        String[] res = new String[5];
        StringBuffer donneesBinaireImage = new StringBuffer();
        StringBuffer commentaire = new StringBuffer();
        int i = 8; // Commence par Mini-PNG
        boolean foundHeader = false;
        long hauteur = 0;
        long largeur = 0;
        int typePixel = 0;
        if(isMiniPNG(data)){
            while (i < data.length) {
                if (byteToInt(data[i]) == 72) {
                    final long longueurBloc = (data[++i] << 24 | data[++i] << 16 | data[++i] << 8 | data[++i]) & 0xFFFFFFFFL;  // ATTENTION incrementation de i : faire avec i+1, i+2 et incrementer i ) la fin de longueurBloc + 4;
                    if (longueurBloc == 9) {
                        largeur = (data[++i] << 24 | data[++i] << 16 | data[++i] << 8 | data[++i]) & 0xFFFFFFFFL;
                        hauteur = (data[++i] << 24 | data[++i] << 16 | data[++i] << 8 | data[++i]) & 0xFFFFFFFFL;
                        typePixel = data[++i] & 0xFF;


                    } else {
                        System.out.println("longueur du header erroné, vérifier le fichier");
                    }
                    foundHeader = true;

                } else if (byteToInt(data[i]) == 67) {
                    final long longueurBloc = ((data[++i] << 24) | (data[++i] << 16) | (data[++i] << 8) | data[++i]) & 0xFFFFFFFFL;
                    for (int k = 1; k <= longueurBloc; k++) {
                        commentaire.append(Character.toChars(data[++i] & 0xFF));
                    }


                } else if (byteToInt(data[i]) == 68) {
                    final long longueurBloc = ((data[++i] << 24) | (data[++i] << 16) | (data[++i] << 8) | data[++i]) & 0xFFFFFFFFL;
                    for (int k = 1; k <= longueurBloc; k++) {
                        donneesBinaireImage.append(String.format("%8s", Integer.toBinaryString(data[++i] & 0xFF)).replace(' ', '0'));
                    }

                }
                else {
                    if(!foundHeader){
                        System.out.println("Header not found");
                    }
                    System.out.println("Aucun bloc détecté");
                }
                i++;
            }
        } else{
            System.out.println("Mauvais format d'image");
        }
        return new String[]{Long.toUnsignedString(hauteur), Long.toUnsignedString(largeur), Integer.toString(typePixel),  commentaire.toString(), donneesBinaireImage.toString()};
    }

    private static void afficheData(String[] arg){
        long hauteur = 0;
        long largeur = 0;
        int typePixel=-1;
        String donneesBinaireImage = arg[4];
        String commentaire = arg[3];

        try {
            typePixel = Integer.parseInt(arg[2]);
            hauteur = Integer.parseInt(arg[0]);
            largeur = Integer.parseInt(arg[1]);
        }catch(NumberFormatException nfe){
            System.out.println("Type de pixel, hauteur et largeur doivent être des entiers");
            System.exit(0);
        }
        System.out.println("Largeur : " + largeur);
        System.out.println("Hauteur : " + hauteur);
        final String messagetypePixel;
        switch (typePixel) {
            case 0:
                messagetypePixel = "0 (noir et blanc)";
                break;
            case 1:
                messagetypePixel = "1 (niveaux de gris)";
                break;
            case 2:
                messagetypePixel = "2 (palette)";
                break;
            case 3:
                messagetypePixel = "3 (couleurs 24bits)";
                break;
            default:
                messagetypePixel = "Mauvais type de pixel";
        }

        System.out.println("Type de pixel : " + messagetypePixel);
        StringBuffer ligneImage = new StringBuffer();
        for (int k = 1; k <= hauteur; k++) {
            String ligneDonneesBinaireImage = donneesBinaireImage.substring(k * Math.toIntExact(largeur) - Math.toIntExact(largeur), k * Math.toIntExact(largeur));
            for (int l = 0; l < ligneDonneesBinaireImage.length(); l++) {
                if ('1' == ligneDonneesBinaireImage.charAt(l)) {// == car diff de char
                    ligneImage.append(" ");
                } else if ('0' == ligneDonneesBinaireImage.charAt(l)) {
                    ligneImage.append("X");
                }
            }
            ligneImage.append('\n');
        }
        System.out.println("Commentaire : \n" + commentaire);
        System.out.println(ligneImage);

    }

    private static boolean isMiniPNG(byte[] bytes){
        final byte[] temp = new byte[8];
        String sb = "Mini-PNG";
        System.arraycopy(bytes, 0, temp, 0,8);
        String test = new String(temp);
            return test.contentEquals(sb);
    }

    private static int byteToInt(byte b){
        return (b & 255);
    }
}