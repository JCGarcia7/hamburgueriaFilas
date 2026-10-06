package projetoesdlinear;

import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Fast-Food Express - TAD Fila (Queue / FIFO)
 * Projeto para a disciplina de Estruturas de Dados do Prof. Dr. David Buzatto.
 * 
 * Implementa visualmente e na mecânica a LinkedQueue conforme os slides da Aula 03.
 */
public class SimuladorFila extends EngineFrame {

    // =========================================================================
    // 1. ESTRUTURA DE DADOS: TAD FILA (LinkedQueue) CONFORME SLIDES DA AULA 03
    // =========================================================================
    
    public interface Queue<Type> extends Iterable<Type> {
        void enqueue(Type value);
        Type dequeue();
        Type peek();
        void clear();
        boolean isEmpty();
        int getSize();
    }

    private static class Node<Type> {
        Type value;
        Node<Type> next;

        Node(Type value) {
            this.value = value;
            this.next = null;
        }
    }

    public static class LinkedQueue<Type> implements Queue<Type> {
        private Node<Type> start;
        private Node<Type> end;
        private int size;

        public LinkedQueue() {
            this.start = null;
            this.end = null;
            this.size = 0;
        }

        @Override
        public void enqueue(Type value) {
            Node<Type> newNode = new Node<>(value);
            if (isEmpty()) {
                start = newNode;
                end = newNode;
            } else {
                end.next = newNode;
                end = newNode;
            }
            size++;
        }

        @Override
        public Type dequeue() {
            if (isEmpty()) {
                return null;
            }
            Type val = start.value;
            start = start.next;
            size--;
            if (start == null) {
                end = null;
            }
            return val;
        }

        @Override
        public Type peek() {
            if (isEmpty()) {
                return null;
            }
            return start.value;
        }

        @Override
        public void clear() {
            start = null;
            end = null;
            size = 0;
        }

        @Override
        public boolean isEmpty() {
            return start == null;
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public Iterator<Type> iterator() {
            return new Iterator<Type>() {
                private Node<Type> current = start;

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public Type next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    Type val = current.value;
                    current = current.next;
                    return val;
                }
            };
        }
    }

    // =========================================================================
    // 2. MODELOS DO JOGO (PEDIDO, BOTÕES E EFEITOS)
    // =========================================================================

    private static class Pedido {
        int id;
        String nome;
        List<String> receita;

        Pedido(int id, String nome, List<String> receita) {
            this.id = id;
            this.nome = nome;
            this.receita = new ArrayList<>(receita);
        }
    }

    private static class BotaoIngrediente {
        Rectangle bounds;
        String ingrediente;
        String label;
        Color cor;

        BotaoIngrediente(double x, double y, double w, double h, String ingrediente, String label, Color cor) {
            this.bounds = new Rectangle(x, y, w, h);
            this.ingrediente = ingrediente;
            this.label = label;
            this.cor = cor;
        }

        boolean estaSobre(Vector2 mousePos) {
            return CollisionUtils.checkCollisionPointRectangle(mousePos, bounds);
        }
    }

    private static class ParticulaPonto {
        double x, y;
        double velY;
        String texto;
        Color cor;
        double vida;

        ParticulaPonto(double x, double y, String texto, Color cor) {
            this.x = x;
            this.y = y;
            this.velY = -50;
            this.texto = texto;
            this.cor = cor;
            this.vida = 1.2;
        }

        void update(double delta) {
            y += velY * delta;
            vida -= delta;
        }
    }

    // =========================================================================
    // 3. ATRIBUTOS DO JOGO
    // =========================================================================

    private LinkedQueue<Pedido> filaPedidos;
    private final int CAPACIDADE_MAXIMA = 5; // Atingiu 6 = Game Over
    private int contadorIdPedido;

    private double tempoParaSpawn;
    private double contadorTempoSpawn;

    private List<String> bancadaMontagem;
    private List<BotaoIngrediente> botoesIngredientes;
    private Rectangle btnLimpar;
    private Rectangle btnEntregar;

    private int pontuacao;
    private int clientesAtendidos;
    private String feedbackMensagem;
    private Color corFeedback;
    private double feedbackTempo;

    private List<ParticulaPonto> particulas;
    private boolean gameOver;
    private Random random;

    public SimuladorFila() {
        super(980, 680, "Fast-Food Express - Simulador de Fila (FIFO)", 60, true);
    }

    @Override
    public void create() {
        random = new Random();
        particulas = new ArrayList<>();
        bancadaMontagem = new ArrayList<>();

        // Criação dos botões de ingredientes (estilo painel do Showcase)
        botoesIngredientes = new ArrayList<>();
        botoesIngredientes.add(new BotaoIngrediente(50, 530, 95, 45, "Pao", "[1] Pão", new Color(222, 184, 135)));
        botoesIngredientes.add(new BotaoIngrediente(155, 530, 95, 45, "Carne", "[2] Carne", new Color(139, 69, 19)));
        botoesIngredientes.add(new BotaoIngrediente(260, 530, 95, 45, "Queijo", "[3] Queijo", new Color(255, 215, 0)));
        botoesIngredientes.add(new BotaoIngrediente(365, 530, 95, 45, "Alface", "[4] Alface", new Color(60, 179, 113)));
        botoesIngredientes.add(new BotaoIngrediente(470, 530, 95, 45, "Bacon", "[5] Bacon", new Color(178, 34, 34)));

        btnLimpar = new Rectangle(600, 530, 140, 45);
        btnEntregar = new Rectangle(755, 530, 170, 45);

        reiniciarJogo();
    }

    private void reiniciarJogo() {
        filaPedidos = new LinkedQueue<>();
        bancadaMontagem.clear();
        particulas.clear();
        contadorIdPedido = 1;
        pontuacao = 0;
        clientesAtendidos = 0;
        tempoParaSpawn = 5.0;
        contadorTempoSpawn = 0;
        feedbackMensagem = "Atenda o primeiro cliente da fila (start/início)!";
        corFeedback = BLACK;
        feedbackTempo = 3.0;
        gameOver = false;

        // Fila inicial com 2 clientes
        gerarNovoPedido();
        gerarNovoPedido();
    }

    // =========================================================================
    // 4. LÓGICA / UPDATE
    // =========================================================================

    @Override
    public void update(double delta) {
        if (gameOver) {
            if (isKeyPressed(KEY_R)) {
                reiniciarJogo();
            }
            return;
        }

        Vector2 mousePos = getMousePositionPoint();

        // 1. Chegada periódica de clientes (enqueue)
        contadorTempoSpawn += delta;
        if (contadorTempoSpawn >= tempoParaSpawn) {
            contadorTempoSpawn = 0;
            if (filaPedidos.getSize() >= CAPACIDADE_MAXIMA + 1) {
                gameOver = true;
                feedbackMensagem = "GAME OVER: A fila excedeu o limite do restaurante!";
                corFeedback = RED;
            } else {
                gerarNovoPedido();
                if (filaPedidos.getSize() > CAPACIDADE_MAXIMA) {
                    gameOver = true;
                    feedbackMensagem = "GAME OVER: Clientes cansaram de esperar na fila!";
                    corFeedback = RED;
                }
            }
        }

        // 2. Atalhos de Teclado para montagem rápida
        if (isKeyPressed(KEY_ONE) || isKeyPressed(KEY_KP_1)) adicionarIngrediente("Pao");
        if (isKeyPressed(KEY_TWO) || isKeyPressed(KEY_KP_2)) adicionarIngrediente("Carne");
        if (isKeyPressed(KEY_THREE) || isKeyPressed(KEY_KP_3)) adicionarIngrediente("Queijo");
        if (isKeyPressed(KEY_FOUR) || isKeyPressed(KEY_KP_4)) adicionarIngrediente("Alface");
        if (isKeyPressed(KEY_FIVE) || isKeyPressed(KEY_KP_5)) adicionarIngrediente("Bacon");

        if (isKeyPressed(KEY_C)) limparBancada();
        if (isKeyPressed(KEY_SPACE) || isKeyPressed(KEY_ENTER)) entregarPedido();

        // 3. Interação via Mouse
        if (isMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            for (BotaoIngrediente b : botoesIngredientes) {
                if (b.estaSobre(mousePos)) {
                    adicionarIngrediente(b.ingrediente);
                    break;
                }
            }
            if (CollisionUtils.checkCollisionPointRectangle(mousePos, btnLimpar)) {
                limparBancada();
            }
            if (CollisionUtils.checkCollisionPointRectangle(mousePos, btnEntregar)) {
                entregarPedido();
            }
        }

        // 4. Atualizar partículas flutuantes
        for (int i = particulas.size() - 1; i >= 0; i--) {
            ParticulaPonto p = particulas.get(i);
            p.update(delta);
            if (p.vida <= 0) {
                particulas.remove(i);
            }
        }

        // 5. Timer de feedback
        if (feedbackTempo > 0) {
            feedbackTempo -= delta;
            if (feedbackTempo <= 0 && !gameOver) {
                feedbackMensagem = "Aguardando montagem do próximo pedido...";
                corFeedback = DARKGRAY;
            }
        }
    }

    private void gerarNovoPedido() {
        int tipo = random.nextInt(5);
        Pedido p;
        switch (tipo) {
            case 0 -> p = new Pedido(contadorIdPedido++, "X-Burger", List.of("Pao", "Carne", "Queijo", "Pao"));
            case 1 -> p = new Pedido(contadorIdPedido++, "X-Salada", List.of("Pao", "Carne", "Queijo", "Alface", "Pao"));
            case 2 -> p = new Pedido(contadorIdPedido++, "X-Bacon", List.of("Pao", "Carne", "Queijo", "Bacon", "Pao"));
            case 3 -> p = new Pedido(contadorIdPedido++, "X-Duplo", List.of("Pao", "Carne", "Carne", "Queijo", "Pao"));
            default -> p = new Pedido(contadorIdPedido++, "X-Tudo", List.of("Pao", "Carne", "Queijo", "Alface", "Bacon", "Pao"));
        }
        filaPedidos.enqueue(p);
    }

    private void adicionarIngrediente(String ing) {
        if (bancadaMontagem.size() < 7) {
            bancadaMontagem.add(ing);
        }
    }

    private void limparBancada() {
        bancadaMontagem.clear();
        definirFeedback("Bancada limpa!", DARKGRAY);
    }

    private void entregarPedido() {
        if (filaPedidos.isEmpty()) {
            definirFeedback("A fila está vazia!", RED);
            return;
        }

        // REGRA ESTRUTURAL: Apenas o primeiro (peek) é atendido!
        Pedido primeiro = filaPedidos.peek();

        if (receitaConfere(bancadaMontagem, primeiro.receita)) {
            // dequeue() -> retira da cabeça da fila
            Pedido atendido = filaPedidos.dequeue();
            pontuacao += 150;
            clientesAtendidos++;
            bancadaMontagem.clear();
            particulas.add(new ParticulaPonto(getScreenWidth() / 2, 280, "+150 Pontos!", GREEN));
            definirFeedback("Pedido #" + atendido.id + " (" + atendido.nome + ") ATENDIDO COM SUCESSO!", GREEN);

            if (tempoParaSpawn > 2.5) {
                tempoParaSpawn -= 0.15;
            }
        } else {
            pontuacao = Math.max(0, pontuacao - 40);
            particulas.add(new ParticulaPonto(getScreenWidth() / 2, 280, "-40 (Pedido Incorreto)", RED));
            definirFeedback("ERRO: O primeiro cliente quer: " + primeiro.receita, RED);
        }
    }

    private boolean receitaConfere(List<String> montado, List<String> receita) {
        if (montado.size() != receita.size()) return false;
        for (int i = 0; i < montado.size(); i++) {
            if (!montado.get(i).equals(receita.get(i))) return false;
        }
        return true;
    }

    private void definirFeedback(String msg, Color cor) {
        feedbackMensagem = msg;
        corFeedback = cor;
        feedbackTempo = 2.5;
    }

    // =========================================================================
    // 5. RENDERIZAÇÃO / DRAW
    // =========================================================================

    @Override
    public void draw() {
        clearBackground(WHITE);

        // Barra Superior (HUD)
        fillRectangle(0, 0, getScreenWidth(), 45, new Color(245, 245, 245));
        drawLine(0, 45, getScreenWidth(), 45, LIGHTGRAY);
        drawText("FAST-FOOD EXPRESS [TAD FILA - FIFO]", 20, 12, 20, BLACK);
        drawText("Pontos: " + pontuacao, 640, 12, 20, BLUE);
        drawText("Atendidos: " + clientesAtendidos, 790, 12, 20, DARKGRAY);

        // 1. Esteira Horizontal da Fila
        desenharEstruturaFila();

        // 2. Divisória
        drawLine(0, 260, getScreenWidth(), 260, LIGHTGRAY);

        // 3. Bancada de Montagem Central (Hambúrguer em Camadas)
        desenharBancada();

        // 4. Botões e Controles
        desenharBotoes();

        // 5. Mensagem de Feedback
        int wMsg = measureText(feedbackMensagem, 18);
        drawText(feedbackMensagem, (getScreenWidth() - wMsg) / 2, 630, 18, corFeedback);

        // 6. Partículas
        for (ParticulaPonto p : particulas) {
            drawText(p.texto, p.x - measureText(p.texto, 18) / 2, p.y, 18, p.cor);
        }

        // 7. FPS (estilo showcase)
        drawFPS(10, 10);

        // 8. Game Over Screen
        if (gameOver) {
            fillRectangle(0, 0, getScreenWidth(), getScreenHeight(), ColorUtils.colorAlpha(BLACK, 0.75));
            fillRoundRectangle(240, 200, 500, 280, 20, WHITE);
            drawRoundRectangle(240, 200, 500, 280, 20, RED);

            drawText("GAME OVER!", 400, 230, 32, RED);
            drawText("A fila acumulou muitos clientes insatisfeitos!", 285, 280, 18, BLACK);
            drawText("Clientes Atendidos: " + clientesAtendidos, 370, 320, 20, BLUE);
            drawText("Pontuação Final: " + pontuacao, 385, 360, 20, GOLD);
            drawText("Pressione [R] para reiniciar o turno", 330, 420, 18, DARKGREEN);
        }
    }

    private void desenharEstruturaFila() {
        drawText("ESTEIRA DE PEDIDOS (FILA ENCADEADA):", 20, 58, 16, BLACK);
        drawText("Capacidade: " + filaPedidos.getSize() + " / " + CAPACIDADE_MAXIMA, 780, 58, 16,
                filaPedidos.getSize() >= CAPACIDADE_MAXIMA ? RED : DARKGRAY);

        int xBase = 80;
        int yBox = 95;
        int boxW = 125;
        int boxH = 135;
        int espaco = 45;

        // Visualização dos ponteiros 'início' e 'fim' com símbolo de aterramento se vazia
        if (filaPedidos.isEmpty()) {
            drawText("Fila Vazia!", 380, 140, 22, DARKGRAY);

            // Ponteiros para terra (conforme Slide 3 e 4 da Aula 03)
            desenharPonteiroComTerra("início", 280, 120);
            desenharPonteiroComTerra("fim", 480, 120);
            return;
        }

        Node<Pedido> atual = filaPedidos.start;
        int i = 0;

        while (atual != null) {
            int x = xBase + i * (boxW + espaco);

            if (i == 0) {
                // PRIMEIRO ELEMENTO (FRENTE / HEAD) - Destaque amarelo
                fillRoundRectangle(x, yBox, boxW, boxH, 10, new Color(255, 248, 180));
                drawRoundRectangle(x, yBox, boxW, boxH, 10, GOLD);

                fillRoundRectangle(x, yBox - 22, boxW, 20, 6, GOLD);
                drawText("ATENDER AGORA", x + 10, yBox - 18, 12, BLACK);

                // Ponteiro 'início' apontando para o primeiro nó
                drawText("início (start)", x + 20, yBox + boxH + 8, 13, RED);
                drawLine(x + boxW / 2, yBox + boxH + 6, x + boxW / 2, yBox + boxH, RED);
                desenharSeta(x + boxW / 2, yBox + boxH, 6, 90, RED);
            } else {
                fillRoundRectangle(x, yBox, boxW, boxH, 10, new Color(248, 249, 250));
                drawRoundRectangle(x, yBox, boxW, boxH, 10, DARKGRAY);
                drawText("Posição #" + (i + 1), x + 30, yBox + boxH + 8, 12, DARKGRAY);
            }

            // Se for o último elemento da fila, desenha o ponteiro 'fim' (end)
            if (atual.next == null) {
                drawText("fim (end)", x + 30, yBox - 20, 13, BLUE);
                drawLine(x + boxW / 2, yBox - 6, x + boxW / 2, yBox, BLUE);
                desenharSeta(x + boxW / 2, yBox, 6, 270, BLUE);
            }

            // Dados do Pedido
            Pedido p = atual.value;
            drawText("#" + p.id + " " + p.nome, x + 8, yBox + 10, 14, BLACK);
            drawLine(x + 5, yBox + 28, x + boxW - 5, yBox + 28, LIGHTGRAY);

            int yIng = yBox + 36;
            for (String ing : p.receita) {
                drawText("• " + ing, x + 10, yIng, 12, BLACK);
                yIng += 16;
            }

            // Setinha encadeada ligando ao próximo nó (ponteiro next)
            if (atual.next != null) {
                int xSetaIni = x + boxW;
                int xSetaFim = xSetaIni + espaco;
                int ySeta = yBox + boxH / 2;
                drawLine(xSetaIni, ySeta, xSetaFim, ySeta, BLUE);
                desenharSeta(xSetaFim, ySeta, 8, 0, BLUE);
                drawText("next", xSetaIni + 8, ySeta - 14, 11, BLUE);
            }

            atual = atual.next;
            i++;
        }
    }

    private void desenharBancada() {
        drawText("BANCADA DE MONTAGEM (Prato Atual)", 50, 275, 18, BLACK);

        // Prato / Bandeja central
        fillRoundRectangle(50, 305, 875, 205, 15, new Color(250, 250, 250));
        drawRoundRectangle(50, 305, 875, 205, 15, LIGHTGRAY);

        if (bancadaMontagem.isEmpty()) {
            drawText("O prato está vazio. Adicione os ingredientes usando os botões ou o teclado [1 a 5]!", 140, 400, 16, GRAY);
        } else {
            // Desenho do lanche montado em camadas visuais (centro da bancada)
            int burgerX = 220;
            int yCamada = 460;

            for (String ing : bancadaMontagem) {
                switch (ing) {
                    case "Pao" -> {
                        fillRoundRectangle(burgerX, yCamada, 180, 22, 10, new Color(222, 184, 135));
                        drawRoundRectangle(burgerX, yCamada, 180, 22, 10, BLACK);
                        drawText("PÃO", burgerX + 75, yCamada + 4, 12, WHITE);
                    }
                    case "Carne" -> {
                        fillRoundRectangle(burgerX + 5, yCamada, 170, 18, 6, new Color(110, 50, 10));
                        drawRoundRectangle(burgerX + 5, yCamada, 170, 18, 6, BLACK);
                        drawText("HAMBÚRGUER", burgerX + 45, yCamada + 2, 12, WHITE);
                    }
                    case "Queijo" -> {
                        fillRectangle(burgerX + 8, yCamada, 164, 12, new Color(255, 215, 0));
                        drawRectangle(burgerX + 8, yCamada, 164, 12, BLACK);
                    }
                    case "Alface" -> {
                        fillRectangle(burgerX + 3, yCamada, 174, 14, new Color(60, 179, 113));
                        drawRectangle(burgerX + 3, yCamada, 174, 14, BLACK);
                    }
                    case "Bacon" -> {
                        fillRectangle(burgerX + 15, yCamada, 150, 12, new Color(178, 34, 34));
                        drawRectangle(burgerX + 15, yCamada, 150, 12, BLACK);
                    }
                }
                yCamada -= 20; // Sobe empilhando
            }

            // Resumo textual ao lado do lanche montado
            drawText("Sequência Atual:", 480, 325, 14, DARKGRAY);
            int yTxt = 350;
            for (int j = 0; j < bancadaMontagem.size(); j++) {
                drawText((j + 1) + "º camada: " + bancadaMontagem.get(j), 500, yTxt, 14, BLACK);
                yTxt += 18;
            }
        }
    }

    private void desenharBotoes() {
        Vector2 mousePos = getMousePositionPoint();

        // Botões de ingredientes
        for (BotaoIngrediente b : botoesIngredientes) {
            boolean hover = b.estaSobre(mousePos);
            fillRoundRectangle(b.bounds.x, b.bounds.y, b.bounds.width, b.bounds.height, 8, hover ? b.cor.brighter() : b.cor);
            drawRoundRectangle(b.bounds.x, b.bounds.y, b.bounds.width, b.bounds.height, 8, BLACK);
            int largTexto = measureText(b.label, 13);
            drawText(b.label, b.bounds.x + (b.bounds.width - largTexto) / 2, b.bounds.y + 16, 13, BLACK);
        }

        // Botão [C] Limpar
        boolean hoverLimpar = CollisionUtils.checkCollisionPointRectangle(mousePos, btnLimpar);
        fillRoundRectangle(btnLimpar.x, btnLimpar.y, btnLimpar.width, btnLimpar.height, 8, hoverLimpar ? new Color(255, 150, 150) : new Color(255, 190, 190));
        drawRoundRectangle(btnLimpar.x, btnLimpar.y, btnLimpar.width, btnLimpar.height, 8, BLACK);
        drawText("[C] Limpar Prato", btnLimpar.x + 15, btnLimpar.y + 16, 14, BLACK);

        // Botão [ESPAÇO] Entregar
        boolean hoverEntregar = CollisionUtils.checkCollisionPointRectangle(mousePos, btnEntregar);
        fillRoundRectangle(btnEntregar.x, btnEntregar.y, btnEntregar.width, btnEntregar.height, 8, hoverEntregar ? new Color(130, 230, 150) : new Color(160, 240, 180));
        drawRoundRectangle(btnEntregar.x, btnEntregar.y, btnEntregar.width, btnEntregar.height, 8, BLACK);
        drawText("[ESPAÇO] Entregar (Peek)", btnEntregar.x + 10, btnEntregar.y + 16, 14, BLACK);
    }

    // Desenha o símbolo clássico de aterramento das aulas do Buzatto (Slide 3 e 4)
    private void desenharPonteiroComTerra(String label, int x, int y) {
        drawText(label, x - measureText(label, 14) / 2, y, 14, DARKGRAY);
        drawLine(x, y + 16, x, y + 40, DARKGRAY);
        desenharSeta(x, y + 40, 6, 270, DARKGRAY);
        // Linhas de aterramento
        drawLine(x - 12, y + 46, x + 12, y + 46, DARKGRAY);
        drawLine(x - 8, y + 50, x + 8, y + 50, DARKGRAY);
        drawLine(x - 4, y + 54, x + 4, y + 54, DARKGRAY);
    }

    // Método desenharSeta idêntico ao do repositório / SimuladorPilha
    private void desenharSeta(double x, double y, double tamanho, double graus, Color cor) {
        drawLine(x, y, x + Math.cos(Math.toRadians(graus - 135)) * tamanho, y + Math.sin(Math.toRadians(graus - 135)) * tamanho, cor);
        drawLine(x, y, x + Math.cos(Math.toRadians(graus + 135)) * tamanho, y + Math.sin(Math.toRadians(graus + 135)) * tamanho, cor);
    }

    public static void main(String[] args) {
        new SimuladorFila();
    }
}