import java.io.*;
import java.text.ParseException;

public class CriadorCapitulos {

    public static void gerarCapitulos() throws IOException, ParseException {
        String csv = "dataset/capitulos.csv";
        String binario = "dataset/capitulos.db";

        File arquivoBinario = new File(binario);

        // Se o arquivo já existir, não faz nada
        if (arquivoBinario.exists()) {
            return;
        }

        // Abre o arquivo CSV para leitura e o arquivo binário para escrita
        BufferedReader br = new BufferedReader(new FileReader(csv));
        RandomAccessFile raf = new RandomAccessFile(binario, "rw");

        String linha;

        // Reserva 4 bytes para o último ID inserido
        raf.writeInt(0);

        while ((linha = br.readLine()) != null) {
            String[] campos = AuxFuncoes.separarPorVirgula(linha);

            Short numeroCapitulo = Short.parseShort(campos[0]);
            int id = numeroCapitulo;
            Short volume = Short.parseShort(campos[1]);
            String nome = campos[2];
            String[] titulos = { campos[3], campos[4] };
            Short paginas = Short.parseShort(campos[5]);
            String data = AuxFuncoes.formatarData(campos[6]);
            String episodio = campos[7];

            Capitulo capitulo = new Capitulo(id, numeroCapitulo, volume, nome, titulos, paginas, data, episodio);
            byte[] dataBytes = capitulo.toByteArray();

            // funções para escrever no arquivo binário
            AuxFuncoes.escreverCapitulo(dataBytes, raf.length());
            AuxFuncoes.IncrementaUltimoIdInserido();
        }

        // Fecha os recursos abertos
        br.close();
        raf.close();

        System.out.println("Importação concluída com sucesso!");
    }
}
