package com.example.memorycanvas;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class Palette {
    int[] colors;
    Queue<Integer> colorsQueue;

    private void RandomizeColors() {
        Random r = new Random();

        for (int i = colors.length - 1; i > 0; i--) {
            int j = r.nextInt(i);

            int tmpColor = colors[i];
            colors[i] = colors[j];
            colors[j] = tmpColor;
        }
    }

    public int getNextColor() {
        return colorsQueue.remove();
    }

    public Palette() {
        colors = new int[]{
                Color.RED,
                Color.RED,
                Color.argb(255, 244, 107, 13),
                Color.argb(255, 244, 107, 13),
                Color.YELLOW,
                Color.YELLOW,
                Color.GREEN,
                Color.GREEN,
                Color.argb(255, 6, 209, 251),
                Color.argb(255, 6, 209, 251),
                Color.BLUE,
                Color.BLUE,
                Color.argb(255, 148,0,211),
                Color.argb(255, 148,0,211)
        };

        RandomizeColors();

        colorsQueue = new LinkedList<>();

        for (int i = 0; i < colors.length; i++) {
            colorsQueue.add(colors[i]);
        }
    }
}

class Card {
    Paint p = new Paint();
    int color, backColor = Color.DKGRAY;
    boolean isOpen = false; // цвет карты
    float x, y, width, height;

    public Card(float x, float y, float width, float height, int color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }



    public void draw(Canvas c) {
        // нарисовать карту в виде цветного прямоугольника
        if (isOpen) {
            p.setColor(color);
        } else {
            p.setColor(backColor);
        }

        c.drawRect(x,y, x+width, y+height, p);
    }

    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;

            return true;
        }

        return false;
    }

}

public class TilesView extends View {
    // пауза для запоминания карт
    final int PAUSE_LENGTH = 2; // в секундах
    boolean isOnPauseNow = false;
    final float cardWidth = 150f;
    final float cardHeight = 200f;
    final float cardMargin = 100f;
    float marginLeft;

    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Card> cardsOpen = new ArrayList<>();

    Palette palette;

    int width, height; // ширина и высота канвы

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        palette = new Palette();

        // As we have to know both screen's width and height - do calculation
        // as soon as screen measurements are ready.
        getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = getWidth();
                height = getHeight();
                marginLeft = width / 2f - cardMargin / 2 - cardWidth;

                for (int i = 0, colInc = 0; i < 14; i++) {
                    boolean isSecond = i % 2 != 0;
                    float xOffset = isSecond ? cardWidth + 100 + marginLeft : marginLeft;
                    float yOffset = cardHeight * colInc + 50 * colInc;

                    colInc += isSecond ? 1 : 0;
                    cards.add(new Card(xOffset, yOffset, cardWidth, cardHeight, palette.getNextColor()));
                }

                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Card c: cards) {
            c.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 3) получить координаты касания
        int x = (int) event.getX();
        int y = (int) event.getY();

        // 4) определить тип события
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            // палец коснулся экрана
            for (Card card: cards) {

                if (cardsOpen.size() == 0 && card.flip(x, y)) {
                    Log.i("fco", Boolean.toString(card.isOpen));
                    cardsOpen.add(card);
                    invalidate();

                    return true;
                }


                // перевернуть карту с задержкой
                else if (cardsOpen.size() == 1 && card.flip(x, y)) {

                    Log.i("sco", Boolean.toString(card.isOpen));
                    cardsOpen.add(card);
                    invalidate();

                    // 1) если открылись карты одинакового цвета, удалить их из списка
                    // например написать функцию, checkOpenCardsEqual
                    if (checkIfCardsEqual()) {
                        cards.removeAll(cardsOpen);
                        checkIfGameIsOver();
                        cardsOpen.clear();
                        invalidate();
                    } else {
                        Log.i("at", "Async task running");
                        PauseTask task = new PauseTask();
                        isOnPauseNow = true;
                        task.execute(PAUSE_LENGTH);
                    }

                    return true;

                }

            }
        }

        return true;
    }

    public boolean checkIfCardsEqual() {
        // Although we have to check only 2 cards,
        // still we use loop to check equality in case
        // when there're more than 2 cards.
        int currentColor = cardsOpen.get(0).color;
        int numOfCardsOpen = cardsOpen.size();

        for (int i = 1; i < numOfCardsOpen; i++) {
            if (currentColor != cardsOpen.get(i).color) {
                return false;
            }
        }

        return true;
    }

    public void checkIfGameIsOver() {
        if (cards.isEmpty()) {
            Context mainActivity = getContext();
            Intent finish = new Intent(mainActivity, GameOverActivity.class);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }

            mainActivity.startActivity(finish);
        }
    }

    public void newGame() {
        // запуск новой игры
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {}
            return null;
        }

        // после паузы, перевернуть все карты обратно
        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }

            cardsOpen.clear();

            isOnPauseNow = false;

            invalidate();

        }
    }
}