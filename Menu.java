
import java.io.*;

public class Menu {

    public static final String BD = "dataset/capitulos.db";

    public static void menu(TreeBplus arvore, HashEstendido hash) throws IOException {
        while (true) {
            MyIO.println("\n--- Menu CRUD Capitulo ---");
            MyIO.println("1. Criar Capitulo");
            MyIO.println("2. Ler Um Capitulo");
            MyIO.println("3. Ler Multiplos Capitulos");
            MyIO.println("4. Atualizar Capitulo");
            MyIO.println("5. Deletar Capitulo");
            MyIO.println("6. Sair");

            MyIO.print("Escolha uma opcao: ");
            int opcao = MyIO.readInt();

            switch (opcao) {
                case 1 -> {
                    if (criarCapitulo(AuxFuncoes.CriarNovoCapitulo(), arvore, hash)) {
                        MyIO.println("Criado com sucesso");
                    } else {
                        MyIO.println("Falhou na criacao");
                    }
                }
                case 2 -> {
                    if (!lerCapitulo(AuxFuncoes.qualID(), arvore, hash)) {
                        MyIO.println("Nao encontrado");
                    }
                }
                case 3 ->
                    lerCapitulos(AuxFuncoes.PerguntaQTD_ID(), arvore, hash);

                case 4 -> {
                    if (atualizarCapitulo(AuxFuncoes.qualID(), arvore, hash)) {
                        MyIO.println("Atualizado com sucesso");
                    } else {
                        MyIO.println("Falhou na atualizacao");
                    }
                }
                case 5 -> {
                    if (deletarCapitulo(AuxFuncoes.qualID(), arvore, hash)) {
                        MyIO.println("Excluido com sucesso");
                    } else {
                        MyIO.println("Falhou na exclusao");
                    }
                }
                case 6 -> {
                    MyIO.println("Saindo...");
                    System.exit(0);
                }
                default ->
                    MyIO.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static boolean criarCapitulo(Capitulo capitulo, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw")) {
            byte[] bytes = capitulo.toByteArray();
            long endereco = raf.length(); // Endereço atual onde será escrito

            AuxFuncoes.escreverCapitulo(bytes, endereco);
            AuxFuncoes.IncrementaUltimoIdInserido();

            // Atualiza a árvore B+ com o novo ID e endereço
            arvore.inserir(capitulo.getId(), endereco);

            // Atualiza o arquivo de índice
            arvore.salvarFolhasNoArquivo("dataset/capitulosIndiceArvore.db");

            hash.inserir(capitulo.getId(), endereco);

            hash.construirDoArquivo("dataset/capitulos.db");

            
        }
        return true;
    }

    private static boolean lerCapitulo(int ID, TreeBplus arvore, HashEstendido hash) throws IOException {
        // Buscar nos dois índices
        Long enderecoArvore = arvore.buscar(ID);
        Long enderecoHash = hash.buscar(ID);
    
        // Evita imprimir null diretamente
        MyIO.println("[Arvore B+] Endereco encontrado: " + 
            (enderecoArvore != null ? enderecoArvore : "nao encontrado"));
        MyIO.println("[Hash Estendido] Endereco encontrado: " + 
            (enderecoHash != null ? enderecoHash : "nao encontrado"));
    
        // Prioridade: Hash, se não existir, usa da Árvore
        Long endereco = (enderecoHash != null) ? enderecoHash : enderecoArvore;
    
        // ✅ ADICIONE ESSA VERIFICAÇÃO
        if (endereco == null) {
            MyIO.println("ID não encontrado em nenhum índice.");
            return false;
        }
    
        try (RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw")) {
            raf.seek(endereco);
            byte valido = raf.readByte();
            int tamanhoVetor = raf.readInt();
    
            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                raf.readFully(byteArray);
    
                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);
    
                MyIO.println(capitulo.toString());
                return true;
            }
        }
    
        return false;
    }
    
    

    private static void lerCapitulos(int[] ids, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw")) {
            for (int id : ids) {
                Long enderecoArvore = arvore.buscar(id);
                Long enderecoHash = hash.buscar(id);
    
                MyIO.println("\n[ID " + id + "]");
                MyIO.println("  [Árvore B+] Endereço: " + enderecoArvore);
                MyIO.println("  [Hash Estendido] Endereço: " + enderecoHash);
    
                Long endereco = (enderecoHash != null) ? enderecoHash : enderecoArvore;
    
                if (endereco != null) {
                    raf.seek(endereco);
                    byte valido = raf.readByte();
                    int tamanhoVetor = raf.readInt();
    
                    if (valido == 1) {
                        byte[] byteArray = new byte[tamanhoVetor];
                        raf.readFully(byteArray);
    
                        Capitulo capitulo = new Capitulo();
                        capitulo.fromByteArray(byteArray);
    
                        MyIO.println("  Conteúdo:");
                        MyIO.println("  " + capitulo.toString());
                    } else {
                        MyIO.println("  Registro marcado como removido.");
                    }
                } else {
                    MyIO.println("  Não encontrado em nenhum índice.");
                }
            }
        }
    }
    

    private static boolean atualizarCapitulo(int ID, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile RAF = new RandomAccessFile(BD, "rw")) {
            Long posicao = arvore.buscar(ID);

            if (posicao == null) {
                MyIO.println("ID não encontrado na árvore.");
                RAF.close();
                return false;
            }

            RAF.seek(posicao);
            byte valido = RAF.readByte();
            int tamanhoVetor = RAF.readInt();

            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                RAF.readFully(byteArray);

                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);

                if (capitulo.getId() == ID) {
                    Capitulo novoCapitulo = AuxFuncoes.CriarNovoCapitulo();
                    novoCapitulo.setId(ID);

                    byte[] novoByteArray = novoCapitulo.toByteArray();

                    if (novoByteArray.length <= tamanhoVetor) {
                        MyIO.println("Atualização coube no espaço reservado.");
                        RAF.seek(posicao + 5); // 1 byte validação + 4 bytes tamanho
                        RAF.write(novoByteArray);
                        RAF.write(new byte[tamanhoVetor - novoByteArray.length]);
                    } else {
                        MyIO.println("Atualização não coube. Inserido no fim do arquivo.");

                        // Marca como removido
                        RAF.seek(posicao);
                        RAF.writeByte(0);

                        // Escreve no final
                        long novaPosicao = RAF.length();
                        AuxFuncoes.escreverCapitulo(novoByteArray, novaPosicao);

                        // Atualiza o índice na árvore
                        arvore.inserir(ID, novaPosicao);
                    }

                    RAF.close();

                    // Atualiza arquivo de índice
                    arvore.salvarFolhasNoArquivo("dataset/capitulosIndiceArvore.db");
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean deletarCapitulo(int ID, TreeBplus arvore, HashEstendido hash) throws IOException {
        try (RandomAccessFile RAF = new RandomAccessFile(BD, "rw")) {
            RAF.seek(0);
            int UltimoId = RAF.readInt();

            while (RAF.getFilePointer() < RAF.length()) {
                long ponteiro = RAF.getFilePointer();
                byte valido = RAF.readByte();
                int tamanhoVetor = RAF.readInt();

                if (valido == 1) {
                    byte[] byteArray = new byte[tamanhoVetor];
                    RAF.readFully(byteArray);
                    Capitulo capitulo = new Capitulo();
                    capitulo.fromByteArray(byteArray);

                    if (capitulo.getId() == ID) {
                        // Exclusão lógica no arquivo
                        RAF.seek(ponteiro);
                        RAF.writeByte(0);

                        if (ID == UltimoId) {
                            RAF.seek(0);
                            RAF.writeInt(UltimoId - 1);
                        }

                        // --- Atualiza árvore B+ ---
                        arvore.remover(ID); // você deve garantir que este método está implementado
                        arvore.salvarFolhasNoArquivo("dataset/capitulosIndiceArvore.db");

                        RAF.close();
                        return true;
                    }
                } else {
                    RAF.skipBytes(tamanhoVetor);
                }
            }
        }
        return false;
    }

}
