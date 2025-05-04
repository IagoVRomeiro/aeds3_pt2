import java.io.*;
import java.util.*;

public class HashEstendido {

    private static final int TAM_BUCKET = 4;
    private int profundidadeGlobal;
    private List<Bucket> diretorio;
    private final String arquivoIndice = "dataset/capitulosIndiceHash.db";

    public HashEstendido() {
        this.profundidadeGlobal = 1;
        this.diretorio = new ArrayList<>();
        diretorio.add(new Bucket(1));
        diretorio.add(new Bucket(1));
        carregar(); // tenta carregar do arquivo, se existir
    }

    private int hash(int id) {
        return id & ((1 << profundidadeGlobal) - 1);
    }

    public void inserir(int id, long posicao) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);

        if (!bucket.estaCheio()) {
            bucket.adicionar(new RegistroIndice(id, posicao));
        } else {
            dividirBucket(h);
            inserir(id, posicao); // tenta de novo após dividir
        }
        salvar();
    }

    public Long buscar(int id) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);
        for (RegistroIndice r : bucket.registros) {
            if (r.id == id) {
                return r.posicao;
            }
        }
        return null;
    }

    public void remover(int id) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);
        Iterator<RegistroIndice> it = bucket.registros.iterator();
        while (it.hasNext()) {
            RegistroIndice r = it.next();
            if (r.id == id) {
                it.remove();
                salvar();
                return;
            }
        }
    }

    public void construirDoArquivo(String caminhoArquivo) {
        File f = new File(arquivoIndice);
        if (f.exists()) {
            return;
        }
    
        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "r")) {
            raf.seek(4); // pula o cabeçalho do último ID
            while (raf.getFilePointer() < raf.length()) {
                long posicaoRegistro = raf.getFilePointer();
                byte validacao = raf.readByte();
                int tamanhoRegistro = raf.readInt();
    
                if (validacao == 1) {
                    int id = raf.readInt(); // assume que o ID está no início do vetor
                    inserir(id, posicaoRegistro);
                }
    
                raf.seek(posicaoRegistro + 1 + 4 + tamanhoRegistro); // pula para o próximo
            }
        } catch (IOException e) {
            System.err.println("Erro ao construir índice: " + e.getMessage());
        }
    }
    

    private void dividirBucket(int indice) {
        Bucket bucketAntigo = diretorio.get(indice);
        int novaProfundidade = bucketAntigo.profundidadeLocal + 1;

        if (novaProfundidade > profundidadeGlobal) {
            duplicarDiretorio();
        }

        Bucket novoBucket = new Bucket(novaProfundidade);
        bucketAntigo.profundidadeLocal = novaProfundidade;

        List<RegistroIndice> antigos = new ArrayList<>(bucketAntigo.registros);
        bucketAntigo.registros.clear();

        for (int i = 0; i < diretorio.size(); i++) {
            if (diretorio.get(i) == bucketAntigo) {
                if ((i & (1 << (novaProfundidade - 1))) != 0) {
                    diretorio.set(i, novoBucket);
                }
            }
        }

        for (RegistroIndice r : antigos) {
            inserir(r.id, r.posicao);
        }
    }

    private void duplicarDiretorio() {
        int tam = diretorio.size();
        for (int i = 0; i < tam; i++) {
            diretorio.add(diretorio.get(i));
        }
        profundidadeGlobal++;
    }

    private void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoIndice))) {
            oos.writeInt(profundidadeGlobal);
            oos.writeInt(diretorio.size());
            for (Bucket b : diretorio) {
                oos.writeObject(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregar() {
        File f = new File(arquivoIndice);
        if (!f.exists()) {
            return; // se não existir, usa o estado inicial padrão
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            profundidadeGlobal = ois.readInt();
            int tam = ois.readInt();
            diretorio = new ArrayList<>();
            for (int i = 0; i < tam; i++) {
                diretorio.add((Bucket) ois.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Classes internas

    static class RegistroIndice implements Serializable {
        int id;
        long posicao;

        public RegistroIndice(int id, long posicao) {
            this.id = id;
            this.posicao = posicao;
        }
    }

    static class Bucket implements Serializable {
        int profundidadeLocal;
        List<RegistroIndice> registros;

        public Bucket(int profundidade) {
            this.profundidadeLocal = profundidade;
            this.registros = new ArrayList<>();
        }

        public boolean estaCheio() {
            return registros.size() >= TAM_BUCKET;
        }

        public void adicionar(RegistroIndice r) {
            registros.add(r);
        }
    }
}
