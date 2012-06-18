package com.example;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class MineSweeper extends Application {

    private static int BOMB = -2;   // 爆弾あり
    private static int INIT = -1;   // 初期値

    private Label bombCountLabel = new Label("000");
    private Label timeCountLabel = new Label("000");
    private GridLayout gamePanel = new GridLayout();
    private final Button resetButton = new Button();
    private CheckBox rightClick = new CheckBox("right");

    private int[] cellData; //マス目のデータ
    private Button[] cell;

    //ゲーム盤の情報を保持する変数（0.行数、1.列数、2.地雷数）
    private int[] gameStatus = {8, 8, 5};
    private int row;
    private int col;


    @Override
    public void init() {
        Window mainWindow = new Window("Vaadin MineSweeper");
        MenuBar menuBar = new MenuBar();
        setMainWindow(mainWindow);
        Layout mainPanel = new VerticalLayout();
        Layout menuPanel = new HorizontalLayout();
        menuPanel.addComponent(bombCountLabel);

        menuPanel.addComponent(resetButton);
        resetButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                initGame();
            }
        });
        menuPanel.addComponent(timeCountLabel);
        menuPanel.addComponent(rightClick);

        mainPanel.addComponent(menuPanel);
        mainPanel.addComponent(gamePanel);
        mainWindow.addComponent(mainPanel);

        initGame();
    }


    // ゲームを初期化する
    private void initGame() {
        resetButton.setIcon(new ThemeResource("../image/happy.gif"));

        row = gameStatus[0];
        col = gameStatus[1];
        int bomb = gameStatus[2];

        //マス目のコンポーネントの作成
        gamePanel.removeAllComponents();
        gamePanel.setRows(row);
        gamePanel.setColumns(col);
        gamePanel.setSpacing(false);

        cell = new Button[row * col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int num = (i * row) + j;
                cell[num] = new Button();
                cell[num].setCaption(String.valueOf(num));
                cell[num].addListener(new ButtonClick(num));
                gamePanel.addComponent(cell[num], j, i);
                gamePanel.setComponentAlignment(cell[num], Alignment.BOTTOM_CENTER);
            }
        }

        // 先頭に地雷を詰めて
        cellData = new int[row * col];
        for (int i = 0; i < cellData.length; i++) cellData[i] = i < bomb ? BOMB : INIT;
        //ランダムに交換して散らばらせる
        for (int i = 0; i < bomb; i++) {
            int idx = (int)(Math.random() * row * col) % cellData.length;
            int tmp = cellData[i];
            cellData[i] = cellData[idx];
            cellData[idx] = tmp;
        }

    }


    /**
     * ボタンクリック時の処理
     */
    class ButtonClick implements Button.ClickListener {
        int number;

        ButtonClick(int number) {
            this.number = number;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            //右クリックのとき
            if (rightClick.booleanValue()) {
                if (cell[number].getCaption().equals("＊")) {
                    cell[number].setCaption("？");
                }
                else if (cell[number].getCaption().equals("？")) {
                    cell[number].setCaption(String.valueOf(number));
                }
                else {
                    cell[number].setCaption("＊");
                }
                return;
            }

            //地雷に当たり
            if (cellData[number] == BOMB) {
                gameOver(number);
            }
            else {
                openCell(number, 0);
            }
        }
    }


    /**
     * ゲームオーバー処理
     */
    private void gameOver(int number) {
        System.out.println("GAME OVER");
        resetButton.setIcon(new ThemeResource("../image/sad.gif"));
        for (int i = 0; i < row * col; i++) {
            if (cellData[i] == BOMB) {
                cell[i].setCaption("");
                cell[i].setIcon(new ThemeResource("../image/bomb.gif"));
            }
            else {
                cell[i].setEnabled(false);
                cell[i].setCaption(String.valueOf(cellData[i]));
            }
        }

    }


    /**
     * 選択されたマス目を開く（cntはデバッグ用）
     */
    private void openCell(int number, int cnt) {
        cnt++;
//{
//StringBuffer sb = new StringBuffer();
//for (int i = 0; i < cnt; i++) sb.append("  ");
//System.out.println(sb.toString() + "openCell(" + number + ", " + cnt + ")");
//}
        int findBomb = 0;   //発見した地雷の数
        boolean left = isLeftCell(number);
        boolean right = isRightCell(number);
        boolean top = isTopCell(number);
        boolean buttom = isButtomCell(number);

        //周囲のセルの値を調べる
        if (left && cellData[number - 1] == BOMB) findBomb++;                    // 左隣にある
        if (right && cellData[number + 1] == BOMB) findBomb++;                   // 右隣にある
        if (top && cellData[(number - col)] == BOMB) findBomb++;                  // 真上にある
        if (top && left && cellData[(number - col) - 1] == BOMB) findBomb++;      // 左上にある
        if (top && right && cellData[(number - col) + 1] == BOMB) findBomb++;     // 右上にある
        if (buttom && cellData[(number + col)] == BOMB) findBomb++;               // 真下にある
        if (buttom && left && cellData[(number + col) - 1] == BOMB) findBomb++;   // 左下にある
        if (buttom && right && cellData[(number + col) + 1] == BOMB) findBomb++;  // 右下にある

        // セルを開く
        cell[number].setCaption(String.valueOf(findBomb));
        cell[number].setEnabled(false);
        cellData[number] = findBomb;

        //周囲のマス目に地雷がないとき，周囲の開いてないセルを開く
        if (findBomb == 0) {
            if (left && cellData[number - 1] == INIT) openCell(number - 1, cnt);                            // 左隣を開く
            if (right && cellData[number + 1] == INIT) openCell(number + 1, cnt);                           // 右隣を開く
            if (top && cellData[number - col] == INIT) openCell(number - col, cnt);                         // 真上を開く
            if (left && top && cellData[(number - col) - 1] == INIT) openCell((number - col) - 1, cnt);     // 左上を開く
            if (top && right && cellData[(number - col) + 1] == INIT) openCell((number - col) + 1, cnt);    // 右上を開く
            if (buttom && cellData[number + col] == INIT) openCell(number + col, cnt);                      // 真下を開く
            if (buttom && left && cellData[(number + col) - 1] == INIT) openCell((number + col) - 1, cnt);  // 左下を開く
            if (buttom && right && cellData[(number + col) + 1] == INIT) openCell((number + col) + 1, cnt); // 右下を開く
        }
    }

    // 真上にセルがあるか
    private boolean isTopCell(int number) {
        return number >= col;
    }

    // 真下にセルがあるか
    private boolean isButtomCell(int number) {
        return number < col * (row - 1);
    }

    // 左隣にセルがあるか
    private boolean isLeftCell(int number) {
        return (number % col) != 0;
    }

    // 右隣にセルがあるか
    private boolean isRightCell(int number) {
        return (number % col) != (col - 1);
    }

}

