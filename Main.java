import java.io.IOException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws ParseException, IOException {

        //Gerar os arquivos capitulos.db e capitulosIndice.db
        CriadorCapitulos.CriadorCapitulos();
        CriadorIndice.gerarIndice();

        //Construir a árvore B+
        TreeBplus arvore = new TreeBplus();
        arvore.construirArvoreDoArquivo("dataset/capitulosIndice.db");


        // Passo 3: Iniciar o menu
        Menu.menu();
    }
}
