import java.io.IOException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws ParseException, IOException {

        // Gerar os arquivos capitulos.db e capitulosIndice.db
        CriadorCapitulos.gerarCapitulos();

        // Construir a árvore B+ e Hash
        System.out.print("Digite a ordem da Árvore B+: ");

        TreeBplus arvore = new TreeBplus(MyIO.readInt());
        arvore.construirArvoreDoArquivo();

        HashEstendido hash = new HashEstendido();
        hash.construirDoArquivoHash();

        // Iniciar o menu
        CRUD.CRUD(arvore, hash);
    }
}
