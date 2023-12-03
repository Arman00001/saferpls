public class SaferPls {

    public static void main(String[] args) {
        MatrixM matrix = new MatrixM();
        char[][] M = new char[16][16],
                inverseM = new char[16][16];
        char[][] Bias_Matrix = new char[33][16];
        Tests test = new Tests();
        // The key which User chooses
        char[] KEY;

        // The message which User chooses
        char[] message;

        // Converting the int Bias words matrix to char matrix
        for (int i = 0; i < 33; i++)
            for (int j = 0; j < 16; j++)
                Bias_Matrix[i][j] = (char) matrix.Bias_Matrix[i][j];

        // Converting the int M to char M
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
                M[i][j] = (char) matrix.M[i][j];

        // Converting the int inverseM to char inverseM
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
                inverseM[i][j] = (char) matrix.inverseM[i][j];

        for (int i = 0; i < test.Key.length; i++) {
            KEY = test.Key[i];
            message = test.PlainText[i];
            char[][] roundKeys = keySchedule(KEY, Bias_Matrix);


            // Call of a encrypting function
            char[] cipher = encryption(roundKeys, message, M, matrix);


            System.out.println("\nPlaintext");
            for (char item : message)
                System.out.print((int) item + ", ");

            System.out.println("\nCipher");
            for (char item : cipher)
                System.out.print((int) item + ", ");

            // Call of a decrypting function
            char[] decipher = decryption(roundKeys, cipher, inverseM, matrix);


            System.out.println("\nDecrypted cipher");
            for (char item : decipher)
                System.out.print((int) item + ", ");


            // Checking whether cipher calculated by the program is the same as the one given in Tests class
            boolean check = true;
            for (int j = 0; j < cipher.length; j++) {
                check = (cipher[j] == test.Cipher[i][j]);
                if (!check) break;
            }
            System.out.println("\nCheck results are: " + check);
        }
    }

    // 2r+1 round keys are required for the encryption and decryption.


    // K_(2i-1), 1<=i<=r
    // One round does 1,4,5,8,9,12,13,16 add bit by bit Mod 2
    // As a result we will obtain some "x". Change the value of x, do the following. x=45^x Mod 257

    // 2,3,6,7,10,11,14,15 added Mod 256
    // As a result we will obtain some "x". Change the value of x, do the following. x=log_45(x)

    // K_(2i)
    // After both, we replace them, which means
    // 2,3,6,7,10,11,14,15 add bit by bit Mod 2
    // 1,4,5,8,9,12,13,16 added Mod 256

    //After both, we get a 16-byte result x, which we multiply by the matrix M and get y. y = xM
    public static char[] encryption(char[][] roundKeys, char[] plaintext, char[][] M, MatrixM matrix) {
        int[] arr1 = {0, 3, 4, 7, 8, 11, 12, 15},
                arr2 = {1, 2, 5, 6, 9, 10, 13, 14};
        int r = (roundKeys.length - 1) / 2;

        char[] cipher = new char[plaintext.length];
        System.arraycopy(plaintext, 0, cipher, 0, cipher.length);

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < arr1.length; j++) {
                cipher[arr1[j]] = (char) (cipher[arr1[j]] ^ roundKeys[2 * i][arr1[j]]);

                cipher[arr1[j]] = matrix.expf[cipher[arr1[j]]];


                cipher[arr2[j]] = (char) Math.floorMod(cipher[arr2[j]] + roundKeys[2 * i][arr2[j]], 256);

                cipher[arr2[j]] = matrix.logf[cipher[arr2[j]]];


                cipher[arr1[j]] = (char) Math.floorMod(cipher[arr1[j]] + roundKeys[2 * i + 1][arr1[j]], 256);

                cipher[arr2[j]] = (char) (cipher[arr2[j]] ^ roundKeys[2 * i + 1][arr2[j]]);
            }


            cipher = multiplyMatrices(cipher, M);
        }

        for (int i = 0; i < arr1.length; i++) {
            cipher[arr1[i]] = (char) (cipher[arr1[i]] ^ roundKeys[2 * r][arr1[i]]);
            cipher[arr2[i]] = (char) ((cipher[arr2[i]] + roundKeys[2 * r][arr2[i]]) % 256);
        }

        return cipher;
    }


    // First operation is post-multiplying y, which means x=yM^(-1), where M^(-1) is the modulo 256 inverse of M

    // K_(2r-2i+2), 1<=i<=r
    // 1,4,5,8,9,12,13,16 subtract Mod 256
    // Then we obtain "x". Change the value of x. x = log_45(x)

    // 2,3,6,7,10,11,14,15 add bit by bit Mod 2
    // Then we obtain "x". Change the value of x. x = 45^x Mod 257


    // Then subtract K_(2r-2i+1) from the acquired x so that
    // 1,4,5,8,9,12,13,16 add bit by bit Mod 2
    // 2,3,6,7,10,11,14,15 subtract Mod 256
    public static char[] decryption(char[][] roundKeys, char[] cipher, char[][] inverseM, MatrixM matrix) {
        int[] arr1 = {0, 3, 4, 7, 8, 11, 12, 15},
                arr2 = {1, 2, 5, 6, 9, 10, 13, 14};
        int r = (roundKeys.length - 1) / 2;

        char[] decipher = new char[cipher.length];
        System.arraycopy(cipher, 0, decipher, 0, decipher.length);

        for (int i = 0; i < arr1.length; i++) {
            decipher[arr1[i]] = (char) (decipher[arr1[i]] ^ roundKeys[2 * r][arr1[i]]);
            decipher[arr2[i]] = (char) Math.floorMod(decipher[arr2[i]] - roundKeys[2 * r][arr2[i]], 256);
        }

        for (int i = 0; i < r; i++) {

            decipher = multiplyMatrices(decipher, inverseM);

            for (int j = 0; j < arr1.length; j++) {

                decipher[arr1[j]] = (char) Math.floorMod(decipher[arr1[j]] - roundKeys[2 * r - 2 * i - 1][arr1[j]], 256);
                decipher[arr1[j]] = matrix.logf[decipher[arr1[j]]];

                decipher[arr2[j]] = (char) (decipher[arr2[j]] ^ roundKeys[2 * r - 2 * i - 1][arr2[j]]);
                decipher[arr2[j]] = matrix.expf[decipher[arr2[j]]];


                decipher[arr1[j]] = (char) (decipher[arr1[j]] ^ roundKeys[2 * r - 2 * i - 2][arr1[j]]);

                decipher[arr2[j]] = (char) Math.floorMod(decipher[arr2[j]] - roundKeys[2 * r - 2 * i - 2][arr2[j]], 256);
            }
        }

        return decipher;
    }


    // User chooses 128-bit Key = 16 byte Key, which becomes K1. 16 bytes are loaded to 17-byte key register,
    // where 17th byte is the XOR added sum of the previous 16 bytes.
    // After that each byte of the key-register is rotated leftwards by 3 bit positions
    //
    // K2 is computed by modulo 256 sum of B2 bytes with bytes of
    // Key Register on positions 2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17 respectively
    // Then each byte of Key Register is rotated leftwards by 3 bit positions
    //
    // K3 computed by modulo 256 sum of B3 bytes with bytes of
    // Key Register on positions 3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,1 respectively
    // Then each byte of Key Register is rotated 3 bit position leftwards
    // .
    // .
    // .
    // K17 is computed by modulo 256 sum of B17 bytes with bytes of
    // Key Register on positions 17,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15
    //
    // In case of 192-bit or 256-bit Key, the process is the same just the number of rounds is 33 or 25, not 17
    // and register size is also 33 or 25, not 17
    public static char[][] keySchedule(char[] KEY, char[][] Bias_Matrix) {
        // Initializing keyRegister, which has 17, 25 or 33 bytes depending on the chosen key-size
        char[] keyRegister = new char[KEY.length + 1];
        int length = keyRegister.length;

        keyRegister[0] = KEY[0];
        keyRegister[length - 1] = KEY[0];

        char[][] roundKeysArray = new char[length][16];

        // Setting up Key 1 of roundKeys
        System.arraycopy(KEY, 0, roundKeysArray[0], 0, 16);

        // Setting up keyRegister values
        for (int i = 1; i < KEY.length; i++) {
            keyRegister[i] = KEY[i];
            keyRegister[length - 1] = (char) (keyRegister[length - 1] ^ keyRegister[i]);
        }


        // Variable used to track the starting element during addition process.
        for (int startPoint = 1; startPoint < length; startPoint++) {

            // Performing bit leftward shift 3 times on keyRegister's elements
            for (int i = 0; i < length; i++) {
                keyRegister[i] = (char) (keyRegister[i] << 3);

                if (keyRegister[i] % 255 == 0 && keyRegister[i] != 0) keyRegister[i] = 255;
                else keyRegister[i] = (char) Math.floorMod(keyRegister[i], 255);
            }

            for (int j = 0; j < 16; j++) {

                int point = Math.floorMod(startPoint + j, length);

                // Calculating the addition of element to biased word's byte modulo 256
                roundKeysArray[startPoint][j] = (char) Math.floorMod(keyRegister[point] + Bias_Matrix[startPoint][j], 256);

            }
        }

        return roundKeysArray;
    }


    static char[] multiplyMatrices(char[] firstMatrix, char[][] secondMatrix) {
        char[] result = new char[16];

        for (int col = 0; col < 16; col++) {
            result[col] = multiplyMatricesCell(firstMatrix, secondMatrix, col);
        }
        return result;
    }

    static char multiplyMatricesCell(char[] firstMatrix, char[][] secondMatrix, int col) {
        char cell = 0;
        for (int i = 0; i < secondMatrix.length; i++) {
            cell += firstMatrix[i] * secondMatrix[i][col];
        }
        return (char) (cell % 256);
    }
}