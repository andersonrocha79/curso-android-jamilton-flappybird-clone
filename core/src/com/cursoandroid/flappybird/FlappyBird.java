package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;



// eixo X -> horizontal
// ------------------------------>
// eixo Y -> vertical
// ^
// |
// |

public class FlappyBird extends ApplicationAdapter
{

    // classe utilizada para criar as animações
    private SpriteBatch batch;

    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;

    private BitmapFont fonte;
    private BitmapFont mensagem;
    private BitmapFont level;

    private Circle passaroCirculo;
    private Rectangle retanguloCanoTopo;
    private Rectangle retanguloCanoBaixo;

    //private ShapeRenderer shape;

    private Random numeroRandomico;


    // atributos de configuração
    private float larguraDispositivo;
    private float alturaDispositivo;

    private int estadoJogo = 0; // 0 - jogo não iniciado / 1 - jogo em andamento / 2 - game over

    private int pontuacao = 0;

    private int levelAtual = 1;

    private int velocidadeCano = 0;

    private int tamanhoMinimoCano = 0;

    private float variacao = 0;
    private float velocidadeQueda = 0;
    private float posicaoInicialVertical = 0;
    private float posicaoMovimentoCanoHorizontal = 0;
    private float espacoEntreCanos = 0;
    private float deltaTime = 0;
    private float alturaEntreCanosRandomico = 0;

    float canoBaixoY = 0;
    float canoTopoY = 0;

    private boolean marcouPonto;

    private final static int ESTADO_INICIAL  = 0;
    private final static int ESTADO_JOGANDO  = 1;
    private final static int ESTADO_GAMEOVER = 2;

    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;



    @Override
    public void create () {

        // Gdx.app.log("Create", "inicializado o jogo");

        // Núcleo do jogo
        batch = new SpriteBatch();

        // gera número randomico para controlar onde os canos vão aparecer
        numeroRandomico = new Random();

        // cria o texto para impressão da pontuação
        fonte = new BitmapFont();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(2);

        // cria o texto para impressão da mensagem de final de jogo
        mensagem = new BitmapFont();
        mensagem.setColor(Color.WHITE);
        mensagem.getData().setScale(2);

        // cria o texto para impressão do level
        level = new BitmapFont();
        level.setColor(Color.YELLOW);
        level.getData().setScale(2);

        // objetos geométricos vinculados as imagens para controlar as colisões
        passaroCirculo = new Circle();
        retanguloCanoBaixo = new Rectangle();
        retanguloCanoTopo = new Rectangle();

        //shape = new ShapeRenderer();

        // carrega as imagens disponíveis para o pássaro
        passaros = new Texture[4];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");
        passaros[3] = new Texture("passaro4.png");

        // carrega a imagem de fundo
        fundo = new Texture("fundo.png");

        // carrega a imagem de game over
        gameOver = new Texture("game_over.png");

        // carrega as imagens dos canos
        canoBaixo = new Texture("cano_baixo.png");
        canoTopo = new Texture("cano_topo.png");

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0 );

        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        // viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        // armazena a largura e a altura do dispositivo
        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;

        // armazena a posição inicial vertical dos objetos
        posicaoInicialVertical = alturaDispositivo / 2;

        // armazena a posição inicial dos canos
        posicaoMovimentoCanoHorizontal = larguraDispositivo - 100;

        // armazena o espaço entre os canos
        espacoEntreCanos = 300;
        alturaEntreCanosRandomico = 0;

        // velocidade inicial do cano
        velocidadeCano = 150;

        // tamanho mínimo do cano
        tamanhoMinimoCano = 200;

        // define o estado inicial do jogo
        estadoJogo = ESTADO_INICIAL;

    }

    @Override
    public void render ()
    {

        // atualiza a camera
        camera.update();

        // Limpa os frames anteriores e melhora a performance do jogo
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // armazena a diferença de tempo entre as execuções do método 'RENDER'
        deltaTime = Gdx.graphics.getDeltaTime();

        // verifica o estado do jogo
        if (estadoJogo == ESTADO_INICIAL)
        {
            // o jogo não está iniciado - verificar se apertou a tela inicialmente
            if (Gdx.input.justTouched())
            {
                // tocou a primeira vez, então o jogo será iniciado
                estadoJogo = ESTADO_JOGANDO;
            }
        }
        else
        {

            // incrementa a queda do passaro
            velocidadeQueda++;

            // o passaro vai caindo na velocidade especificada
            // se for passar do canto inferior da tela, para a queda, paralisando no final
            if (posicaoInicialVertical > 0 || velocidadeQueda < 0)
            {
                posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;
            }

            // verifica se o jogo está em execução
            if (estadoJogo == ESTADO_JOGANDO)
            {

                // estado 1 - jogo em andamento

                // define a imagem a ser utilizada no passaro (indice)
                variacao += deltaTime * 10; // calcula a diferença de tempo entre as execuções do render
                if (variacao > 2) variacao = 0;

                // cria a movimentação do cano da esquerda para direita
                posicaoMovimentoCanoHorizontal -= deltaTime * velocidadeCano;

                // verifica se a tela foi tocada para que o pássaro voe.
                if (Gdx.input.justTouched())
                {
                    velocidadeQueda = -10;
                }

                // criação de um novo cano
                // se o cano chegar ao final da tela, ele reinicia novamente no início da tela
                if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth())
                {

                    // inicia o cano no inicio da tela
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;

                    // recebe uma nova configuração de altura a cada execução
                    alturaEntreCanosRandomico = numeroRandomico.nextInt(400) - tamanhoMinimoCano;

                    // sempre que criar um novo cano, define que não marcou ponto
                    marcouPonto = false;

                }

                // verifica se marcou ponto
                if (posicaoMovimentoCanoHorizontal < 120)
                {
                    if (!marcouPonto)
                    {

                        // se o passaro passar pelo cano, incrementa a pontuação
                        pontuacao++;
                        marcouPonto = true;

                        // a cada 10 pontos, incrementa o level
                        if ((pontuacao % 10) == 0)
                        {
                            // incrementa o level
                            levelAtual++;
                        }


                        // a cada 5 pontos, incrementa a dificuldade
                        if ((pontuacao % 5) == 0)
                        {
                            // aumenta a velocidade do cano
                            velocidadeCano = velocidadeCano + 20;
                            // diminiui o espaço entre os canos
                            espacoEntreCanos = espacoEntreCanos - 3;
                        }

                    }
                }

            }
            else
            {
                // tela de game over

                // o passaro para de bater as asas e apareça a imagem de morto
                variacao = 3;

                // se tocar na tela o jogo irá reiniciar
                if (Gdx.input.justTouched())
                {
                    // então o jogo será iniciado
                    estadoJogo = ESTADO_JOGANDO;
                    reiniciaValores();
                }

            }

        }

        canoBaixoY = alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + alturaEntreCanosRandomico;
        canoTopoY = alturaDispositivo/2 + espacoEntreCanos/2 + alturaEntreCanosRandomico;

        // desenha os objetos

        // recupera os dados de projeção da tela atual
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, canoTopoY);
        batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, canoBaixoY);
        batch.draw(passaros[(int)variacao], 120, posicaoInicialVertical);
        fonte.draw(batch, "Pontuação: " + String.valueOf(pontuacao),  15, alturaDispositivo - 100);
        level.draw(batch, "Level: "     + String.valueOf(levelAtual), 15, alturaDispositivo - 50);

        // verifica se o jogo terminou (game over)
        if (estadoJogo == ESTADO_GAMEOVER)
        {
            // fim do jogo
            // mostra a imagem GAMEOVER
            batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
            // mostra a mensagem para reiniciar o jogo
            mensagem.draw(batch, "Toque para reiniciar o jogo...", larguraDispositivo/2 - 200, alturaDispositivo / 2 - gameOver.getHeight()/2);
        }

        batch.end();

        // configura o posiconamento dos objetos geométricos para verificação da colisão
        passaroCirculo.set(120 + passaros[0].getWidth() / 2, posicaoInicialVertical + passaros[0].getHeight()/2, passaros[0].getWidth() / 2);
        retanguloCanoBaixo.set(posicaoMovimentoCanoHorizontal, canoBaixoY, canoBaixo.getWidth(), canoBaixo.getHeight());
        retanguloCanoTopo.set(posicaoMovimentoCanoHorizontal, canoTopoY, canoTopo.getWidth(), canoTopo.getHeight());

        // desenhar as formas na tela para teste
        /*
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
        shape.rect(retanguloCanoBaixo.x, retanguloCanoBaixo.y, retanguloCanoBaixo.width, retanguloCanoBaixo.height);
        shape.rect(retanguloCanoTopo.x, retanguloCanoTopo.y, retanguloCanoTopo.width, retanguloCanoTopo.height);
        shape.setColor(Color.RED);
        shape.end();
        */

        // Teste de GAME OVER
        // Teste de Colisão entre o pássaro e os canos
        // se colidir com os canos, ou se cair no chão ou passar o teto o sistema entre no modo GAME OVER
        if (Intersector.overlaps(passaroCirculo, retanguloCanoBaixo) ||
            Intersector.overlaps(passaroCirculo, retanguloCanoTopo)  ||
            posicaoInicialVertical <= 0                              ||
            posicaoInicialVertical >= alturaDispositivo)
        {
            // altera o estado para gameover
            estadoJogo = ESTADO_GAMEOVER;
        }

    }

    public void reiniciaValores()
    {
        // reinicia a pontuação
        pontuacao  = 0;
        // reinicia o level
        levelAtual = 1;
        // armazena o espaço entre os canos
        espacoEntreCanos = 300;
        alturaEntreCanosRandomico = 0;
        // velocidade inicial do cano
        velocidadeCano = 150;
        // tamanho mínimo do cano
        tamanhoMinimoCano = 200;
        // zera a velocidade de queda para que o passaro volte para o centro
        velocidadeQueda = 0;
        posicaoInicialVertical = alturaDispositivo/2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;

    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height);
    }
}
