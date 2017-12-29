import javax.swing.*;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

import java.util.*;

//操作キャラベース-----------------------------------------------------------

abstract class ChinChinPlayerBase extends JPanel {
    private final int K_UP, K_DOWN, K_LEFT, K_RIGHT, K_WeakAttack, K_StrongAttack;//操作ボタンコード
    protected boolean bK_UP = false, bK_DOWN = false, bK_LEFT = false, bK_RIGHT = false, bK_WeakAttack = false, bK_StrongAttack = false;//操作ボタンのフラグ
    protected boolean is_Jump = false, is_HighJump = false, is_Dash = false, is_Squat = false;//ジャンプ,二段ジャンプ中かダッシュ中かしゃがみ中かどうか

    protected boolean canAttack = true;//攻撃できるかどうか
    protected int interval = 0, attackId = -1;//次攻撃を受け付けるまでの時間を管理する, 前回攻撃idを引き継ぐ(-1はダミーコード)
    protected AttackInfo attackInfo = null;//攻撃情報を収納する

    protected boolean before_Right = false;//直前の左右入力を保存する
    protected boolean canBlock = false;//ガード受付状態の管理
    protected int HP;//キャラの体力
    protected Point2D.Float position; //キャラの座標
    protected float dy, dx;
    protected Point size;//キャラのサイズ
    protected int underLine, rightLine;//ステージ下限右限
    protected int stun, canDash;//行動不自由時間, ダッシュ状態を受け付けている時間
    protected Image nowImage;
    protected Looking look; //右を向いているか




    public ChinChinPlayerBase(int K_UP, int K_DOWN, int K_LEFT, int K_RIGHT, int K_WeakAttack, int K_StrongAttack, float positionX, int sizeX, int sizeY, boolean is_Right){
        //操作ボタンコード
        this.K_UP = K_UP;
        this.K_DOWN = K_DOWN;
        this.K_LEFT = K_LEFT;
        this.K_RIGHT = K_RIGHT;
        this.K_WeakAttack = K_WeakAttack;
        this.K_StrongAttack = K_StrongAttack;

        if(is_Right){
            look = Looking.Right;
        }else{
            look = Looking.Left;
        }

        underLine = ChinChinFighter.SCREEN_HEIGHT * 9 / 10 - sizeY;
        rightLine = ChinChinFighter.SCREEN_WIDTH - sizeX;

        //座標設定
        position = new Point2D.Float(positionX, underLine);
        size = new Point(sizeX, sizeY);
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode == K_UP && !bK_UP){
            if(bK_UP = !bK_DOWN){
                Jump(true);
                canBlock = false;
            }
            dx = 0;
        } else
        if(keyCode == K_DOWN && !bK_DOWN){ if(bK_DOWN = !bK_UP){ Squat(true); } } else
        if(keyCode == K_RIGHT && !bK_RIGHT){
            if(bK_RIGHT = !bK_LEFT){
                Move(true, true);
                canBlock = look == Looking.Left;
            }
        } else
        if(keyCode == K_LEFT && !bK_LEFT){
            if(bK_LEFT = !bK_RIGHT){
                Move(false, true);
                canBlock = look == Looking.Right;
            }
        } else
        if(keyCode == K_WeakAttack && !bK_WeakAttack){
            bK_WeakAttack = true;
            WeakAttack(true);
        } else if(keyCode == K_StrongAttack && !bK_StrongAttack){
            bK_StrongAttack = true;
            StrongAttack(true);
        }
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode == K_UP){ bK_UP = false; } else
        if(keyCode == K_DOWN){ bK_DOWN = false; dx /= 2f;} else
        if(keyCode == K_RIGHT){ bK_RIGHT = false; if(look == Looking.Left){canBlock = false;}} else
        if(keyCode == K_LEFT){ bK_LEFT = false; if(look == Looking.Right){canBlock = false;}} else
        if(keyCode == K_WeakAttack){ bK_WeakAttack = false; } else
        if(keyCode == K_StrongAttack){ bK_StrongAttack = false; }
    }

    //HPを返す
    abstract protected int getHP();
    //HPをセットする
    abstract protected void setHP(int HP);
    //ジャンプモーション
    abstract protected void Jump(boolean Pressed);
    //しゃがみモーション
    abstract protected void Squat(boolean Pressed);
    //弱攻撃
    abstract protected void WeakAttack(boolean Pressed);
    //強攻撃
    abstract protected void StrongAttack(boolean Pressed);
    //移動
    abstract protected void Move(boolean is_Right, boolean Pressed);
    //一連の行動を管理する
    public void Action(){
        //落下処理
        if(is_Jump) {
            canBlock = false;
            position.y += dy;
            dy += 2.0f;
            if (position.y >= underLine) {
                is_Jump = false;
                is_HighJump = false;
                position.y = underLine;
                dx = 0;
                //ここで反転処理を行う
                canBlock = (look == Looking.Left && bK_RIGHT) || (look == Looking.Right && bK_LEFT);
            }
        }
    }

    public Image getNowImage(){
        return nowImage;
    }
    public Point2D.Float getPosition(){
        return position;
    }

    //ダメージ情報を扱う
    public boolean Damage(int damage){
        HP -= damage;
        return HP>0;
    }

    //向き情報を設定し返す
    public Looking setgetLook(Point2D.Float OpponentPos){
       if (!is_Jump){
           if(position.x <= OpponentPos.x){
               return look = Looking.Right;
           }else{
               return look = Looking.Left;
           }
       }
       return look;
    }

    public Looking getLook(){
        return look;
    }

}

//操作キャラA
class FighterA extends ChinChinPlayerBase{

    public FighterA(int K_UP, int K_DOWN, int K_LEFT, int K_RIGHT, int K_WeakAttack, int K_StrongAttack, float positionX, float positionY, int sizeX, int sizeY, boolean is_Right){
        super(K_UP, K_DOWN, K_LEFT, K_RIGHT, K_WeakAttack, K_StrongAttack, positionX, sizeX, sizeY, is_Right);
        HP = 100;//体力セット

        nowImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/playerADammy.png"));
    }

    @Override
    protected int getHP(){
        return HP;
    }

    @Override
    protected void setHP(int HP){
        this.HP = HP;
    }

    @Override
    protected void Jump(boolean Pressed) {
        if(Pressed && (!is_Jump || !is_HighJump)){
            if(!is_Jump){
                is_Jump = true;
            }else{
                is_HighJump = true;
            }
            dy = -30.0f;
        }
    }

    @Override
    protected void Squat(boolean Pressed) {
        if (Pressed){
            if(is_Jump){dx *= 0.5f;}
            is_Dash = false;
        }
        else if(!is_Jump && dx != 0.0f){dx *= 0.3f;}
    }

    @Override
    protected void WeakAttack(boolean Pressed) {
        //Point StartingPoint, Point EndingPoint, int Duration, int Interval, int Damage, int id
        if(canAttack) {
            if (Pressed) {
                switch (attackId) {
                    case -1:
                        attackInfo = new AttackInfo(new Point(10, 10), new Point(40, 40), 200, 100, 10, 1);
                        canAttack = false;
                        break;
                }
            }
        }
    }

    @Override
    protected void StrongAttack(boolean Pressed) {
        if(canAttack) {
            if (Pressed) {
                switch (attackId) {
                    case -1:
                        attackInfo = new AttackInfo(new Point(10, 10), new Point(40, 40), 200, 100, 10, 11);
                        canAttack = false;
                        break;
                }
            }
        }
    }

    @Override
    protected void Move(boolean is_Right ,boolean Pressed) {
        if(before_Right != is_Right){
            canDash = 0;
        }
        if(!is_Dash){
            dx = is_Right ? 7f : -7f;
            if(Pressed && !is_Jump && !bK_DOWN){
                if(canDash > 0){is_Dash = true;}
                canDash = 7;
            }
        }else{
            dx = is_Right ? 20f : -20f;
        }

        before_Right = is_Right;
    }

    @Override
    public void Action() {
        if(stun <= 0){
            if(bK_WeakAttack){WeakAttack(false);}
            else if(bK_StrongAttack){StrongAttack(false);}

            if(bK_LEFT || bK_RIGHT){ Move(bK_RIGHT, false); }else{
                canDash--;
                if(is_Dash){is_Dash = false;}
                if(!is_Jump){dx = 0;}
            }

            if(bK_UP){Jump(false);}
            else if(bK_DOWN){Squat(false);}

            //横方向移動
            position.x += dx;
            if(position.x<0){
                dx = 0;
                position.x = 0;
            }else if(position.x>rightLine){
                dx = 0;
                position.x = rightLine;
            }

            //攻撃情報を扱う
            if(attackInfo != null){
               // attackInfo.;
            }
        }else{
            stun--;
            canAttack = false;
            attackInfo = null;
        }

        super.Action();
    }
}

//方向
enum Looking{
    Right(1f),
    Left(-1f);

    private final float value;

    private Looking(final float value){
        this.value = value;
    }

    public float getValue(){
        return value;
    }
}

//攻撃情報管理クラス
class AttackInfo{
    private Point StartingPoint, EndingPoint;//キャラ座標を基準とする攻撃の始点終点(右基準)
    private int Duration, Interval;//攻撃継続時間, 現在の時間, 攻撃後入力を受け付けない時間 (基本的にはDuration >= Interval、モーションの時間とも一致させたら楽だと思う)
    private int Damage;//ダメージ量
    private int id;//攻撃ID
    private boolean alreadyHit = false;//既に攻撃判定を終えているか

    public AttackInfo(Point StartingPoint, Point EndingPoint, int Duration, int Interval, int Damage, int id){
        this.StartingPoint = StartingPoint;
        this.EndingPoint = EndingPoint;
        this.Duration = Duration;
        this.Damage = Damage;
        this.Interval = Interval;
        this.id = id;
    }

    //アクセサメソッド
    public Point getStartingPoint(){ return StartingPoint; }
    public Point getEndingPoint(){ return EndingPoint; }
    public int getDuration(){ return Duration; }
    public int getDamage(){ return Damage; }

    //継続時間を管理する
    public boolean reduceDuration(){ return Duration-- < 0;}
    //入力受けつけを管理する
    public boolean reduceInterval(){ return Interval-- < 0;}

    //当たり判定の確認を行う
    public boolean judgeHitted(Point p, Point opS, Point opE){
        //pの座標,opの当たり判定の範囲が引数
        boolean isHitted = p.x + EndingPoint.x >= opS.x && p.x + StartingPoint.x <= opE.x &&
                p.y + EndingPoint.y >= opS.y && p.y + StartingPoint.y <= opE.y;
        alreadyHit |= isHitted;
        return isHitted && !alreadyHit;
    }
}

//-------------------------------------------------------画面構成&ゲーム管理クラス

class ChinChinFrameView extends JPanel implements KeyListener{
    private java.util.Timer gameThread;//ゲーム用スレッド
    private ChinChinFighter chinChinFighter;

    private ChinChinPlayerBase player1, player2;
    private Point2D.Float p1position, p2position;
    private Point p1size, p2size;
    private Image p1image, p2image;

    public ChinChinFrameView(ChinChinFighter chinChinFighter){
        this.chinChinFighter = chinChinFighter;
        this.setSize(ChinChinFighter.SCREEN_WIDTH, ChinChinFighter.HEIGHT);
        this.setBackground(new Color(150,255,255));
        this.setLayout(new GridLayout(1,2));

        //プレイヤー1
        p1size = new Point(120, 120);
        player1 = new FighterA(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,  KeyEvent.VK_C, KeyEvent.VK_V, 0, ChinChinFighter.SCREEN_HEIGHT-p1size.y, p1size.x, p1size.y, true);
        this.add(player1);

        //プレイヤー2
        p2size = new Point(120 , 120);
        player2 = new FighterA(KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L,  KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, ChinChinFighter.SCREEN_WIDTH-p2size.x, ChinChinFighter.SCREEN_HEIGHT-p2size.y, p2size.x, p2size.y, false);
        this.add(player2);

        setFocusable(true);
        addKeyListener(this);

        //
        gameThread = new java.util.Timer();
        gameThread.schedule(new TimerTask() {
            @Override
            public void run() {
                player1.Action();
                player2.Action();
                repaint();
            }
        }, 0, 20);
    }

    @Override
    public void paint(Graphics g){
        p1position = player1.getPosition();
        p2position = player2.getPosition();

        //位置を渡してついでにJump判定もやってもらうことにした
        if((p1image = player1.getNowImage()) != null) {
            //(p1 < p2) -> lookingRight
            if(player1.setgetLook(p2position) == Looking.Right){
                g.drawImage(p1image,
                        (int) p1position.x, (int) p1position.y,
                        p1size.x, p1size.y, null);
            }else{
                g.drawImage(p1image,
                        (int) p1position.x + p1size.x, (int) p1position.y,
                        -p1size.x, p1size.y, null);
            }
        }
        if((p2image = player2.getNowImage()) != null) {
            //p2 < p1 -> lookingRight
            if(player2.setgetLook(p1position) == Looking.Right){
                g.drawImage(p2image,
                        (int) p2position.x, (int) p2position.y,
                        p2size.x, p2size.y, null);
            }else{
                g.drawImage(p2image,
                        (int) p2position.x + p2size.x, (int) p2position.y,
                        -p2size.x, p2size.y, null);
            }
        }

        //fillRectでHPを表示してます。
        g.setColor(new Color(0, 200, 0));
        g.fillRect(320-player1.getHP()*3, 10, player1.getHP()*3, 20);
        g.fillRect(400, 10, player2.getHP()*3, 20);

        //当たり判定デバッグ用

        //
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //HP動作がちゃんとできているかの確認用
        char c = e.getKeyChar();
        if(c == 'n'){
            player1.setHP(player1.getHP()-1);
        }else if(c == 'b'){
            player2.setHP(player2.getHP()-1);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        player1.keyPressed(e);
        player2.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player1.keyReleased(e);
        player2.keyReleased(e);
    }
}

public class ChinChinFighter extends JFrame {
    private ChinChinFrameView chinChinFrameView;
    public final static int SCREEN_WIDTH = 720, SCREEN_HEIGHT = 600;

    public ChinChinFighter(){
        chinChinFrameView = new ChinChinFrameView(this);
        this.setSize(SCREEN_WIDTH,  SCREEN_HEIGHT);
        this.add(chinChinFrameView);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);
    }

    public static void main(String argv[]){
        new ChinChinFighter();
    }
}
