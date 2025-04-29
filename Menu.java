
import java.io.*;

public class Menu {

    public static final String BD = "dataset/capitulos.db";

    public static void menu() throws IOException {
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
                    if (criarCapitulo(AuxFuncoes.CriarNovoCapitulo())) {
                        MyIO.println("Criado com sucesso");
                    } else {
                        MyIO.println("Falhou na criacao");
                    }
                }
                case 2 -> {
                    if (!lerCapitulo(AuxFuncoes.qualID())) {
                        MyIO.println("Nao encontrado");
                    }
                }
                case 3 ->
                    lerCapitulos(AuxFuncoes.PerguntaQTD_ID());

                case 4 -> {
                    if (atualizarCapitulo(AuxFuncoes.qualID())) {
                        MyIO.println("Atualizado com sucesso");
                    } else {
                        MyIO.println("Falhou na atualizacao");
                    }
                }
                case 5 -> {
                    if (deletarCapitulo(AuxFuncoes.qualID())) {
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

    private static boolean criarCapitulo(Capitulo capitulo) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw");

        byte[] bytes = capitulo.toByteArray();

        AuxFuncoes.escreverCapitulo(bytes, raf.length());
        AuxFuncoes.IncrementaUltimoIdInserido();

        

        raf.close();
        return true;
    }

    private static boolean lerCapitulo(int ID) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw");

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            byte valido = raf.readByte();
            int tamanhoVetor = raf.readInt();

            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                raf.readFully(byteArray);

                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);

                if (capitulo.getId() == ID) {
                    MyIO.println(capitulo.toString());
                    return true;
                }
            } else {
                raf.skipBytes(tamanhoVetor);
            }
        }

        raf.close();
        return false;
    }

    private static void lerCapitulos(int[] ids) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw");

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            byte valido = raf.readByte();
            int tamanhoVetor = raf.readInt();

            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                raf.readFully(byteArray);

                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);

                for (int id : ids) {
                    if (capitulo.getId() == id) {
                        MyIO.println(capitulo.toString());
                    }
                }
            } else {
                raf.skipBytes(tamanhoVetor);
            }
        }

        raf.close();

    }

    private static boolean atualizarCapitulo(int ID) throws IOException {
        RandomAccessFile RAF = new RandomAccessFile(BD, "rw");
        RAF.seek(4);

        while (RAF.getFilePointer() < RAF.length()) {
            long posicao = RAF.getFilePointer();
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
                        MyIO.println("Atualizacao coube no espaco reservado");
                        RAF.seek(posicao + 5);
                        RAF.write(novoByteArray);

                        RAF.write(new byte[tamanhoVetor - novoByteArray.length]);

                        return true;

                    } else {
                        MyIO.println("Atualizacao nao coube no espaco reservado, inserido no fim do arquivo");

                        RAF.seek(posicao);
                        RAF.writeByte(0);
                        AuxFuncoes.escreverCapitulo(novoByteArray, RAF.length());
                        return true;
                    }

                }

            } else {
                RAF.skipBytes(tamanhoVetor);
            }

        }

        RAF.close();
        return false;
    }

    private static boolean deletarCapitulo(int ID) throws IOException {
        RandomAccessFile RAF = new RandomAccessFile(BD, "rw");

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
                    RAF.seek(ponteiro);
                    RAF.writeByte(0);

                    if (ID == UltimoId) {
                        RAF.seek(0);
                        RAF.writeInt(UltimoId - 1);
                    }

                    RAF.close();
                    return true;

                }
            } else {
                RAF.skipBytes(tamanhoVetor);
            }
        }
        RAF.close();
        return false;
    }
}
