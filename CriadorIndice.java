import java.io.*;

public class CriadorIndice {

    public static void gerarIndice() throws IOException {
        String binario = "dataset/capitulos.db";
        String indice = "dataset/capitulosIndice.db";

        RandomAccessFile raf = new RandomAccessFile(binario, "r");
        DataOutputStream dosIndice = new DataOutputStream(new FileOutputStream(indice));

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long endereco = raf.getFilePointer(); 
            byte lapide = raf.readByte();
            int tamanhoVetor = raf.readInt();
            int id = raf.readInt(); 
            
     

            dosIndice.writeInt(id);
            dosIndice.writeLong(endereco);

            raf.skipBytes(tamanhoVetor - 4);
            
        }

        raf.close();
        dosIndice.close();
    }
}
