import java.io.IOException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws ParseException, IOException {

        // Gerar os arquivos capitulos.db e capitulosIndice.db
        CriadorCapitulos.gerarCapitulos();

        // Construir a árvore B+ e Hash
        System.out.print("Digite a ordem da Árvore B+: ");
        int ordem = MyIO.readInt();

        TreeBplus arvore = new TreeBplus(ordem);
        arvore.construirArvoreDoArquivo("dataset/capitulos.db");

        HashEstendido hash = new HashEstendido();
        hash.construirDoArquivo("dataset/capitulos.db");

        arvore.imprimirFolhas();
        // Iniciar o menu
        Menu.menu(arvore, hash);
    }
}
