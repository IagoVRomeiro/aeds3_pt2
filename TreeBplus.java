import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

class TreeBplus {
    public No raiz;

    public TreeBplus() {
        raiz = new No(true); // raiz começa como folha
    }

    public No getRaiz() {
        return raiz;
    }

    public void inserir(int id, long endereco) {
        No folha = encontrarFolha(raiz, id);
        folha.inserirIdEnderecoOrdenado(id, endereco);

        if (folha.verificaOverflow()) {
            dividirFolha(folha);
        }
    }

    public void construirArvoreDoArquivo(String caminhoArquivo) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(caminhoArquivo))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                long endereco = dis.readLong();
                inserir(id, endereco);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private No encontrarFolha(No noAtual, int id) {
        while (!noAtual.ehFolha()) {
            noAtual = noAtual.proximoNo(id);
        }
        return noAtual;
    }

    private void dividirFolha(No folha) {
        No novoNo = folha.separaRetornaFolha();
        int idPromovido = novoNo.getIds().get(0);

        if (folha == raiz) {
            No novaRaiz = new No(false);
            novaRaiz.getIds().add(idPromovido);
            novaRaiz.getFilhos().add(folha);
            novaRaiz.getFilhos().add(novoNo);
            raiz = novaRaiz;
        } else {
            promover(folha, idPromovido, novoNo);
        }
    }

    private void promover(No filhoAntigo, int idPromovido, No novoFilho) {
        No pai = encontrarPai(raiz, filhoAntigo);
        if (pai == null) return;

        int pos = pai.inserirId(idPromovido);
        pai.getFilhos().add(pos + 1, novoFilho);

        if (pai.verificaOverflow()) {
            dividirInterno(pai);
        }
    }

    private void dividirInterno(No no) {
        No novoNo = no.separaRetorna();
        int idPromovido = no.getIds().remove(no.getIds().size() - 1);

        if (no == raiz) {
            No novaRaiz = new No(false);
            novaRaiz.getIds().add(idPromovido);
            novaRaiz.getFilhos().add(no);
            novaRaiz.getFilhos().add(novoNo);
            raiz = novaRaiz;
        } else {
            promover(no, idPromovido, novoNo);
        }
    }

    private No encontrarPai(No noAtual, No filhoProcurado) {
        if (noAtual.ehFolha() || noAtual.getFilhos().isEmpty()) return null;

        for (No filho : noAtual.getFilhos()) {
            if (filho == filhoProcurado) return noAtual;
            No pai = encontrarPai(filho, filhoProcurado);
            if (pai != null) return pai;
        }
        return null;
    }
}
