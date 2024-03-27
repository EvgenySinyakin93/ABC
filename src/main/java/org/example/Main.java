package org.example;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static ArrayBlockingQueue<String> QueueA = new ArrayBlockingQueue<>(100);
    public static ArrayBlockingQueue<String> QueueB = new ArrayBlockingQueue<>(100);
    public static ArrayBlockingQueue<String> QueueC = new ArrayBlockingQueue<>(100);
    private static Thread textGeneration;

    public static void main(String[] args) {
        textGeneration = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    QueueA.put(text);
                    QueueB.put(text);
                    QueueC.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            textGeneration.start();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Thread a = getThread(QueueA, 'a');
            Thread b = getThread(QueueB, 'b');
            Thread c = getThread(QueueC, 'c');

            a.start();
            b.start();
            c.start();

            try {
                a.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                b.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                c.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
    private static Thread getThread(BlockingQueue<String> queue, char letter) {
        return new Thread(() -> {
            int max = 0;
            try {
                max = findMax(queue, letter);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Максимальное количество симловов " + letter + "всего символов " + max);
        });
    }

    private static int findMax(BlockingQueue<String> queue, char letter) throws InterruptedException {
        int max = 0;
        int count = 0;
        String text;
        try {
            while (textGeneration.isAlive()) {
                text = queue.take();
                for (char c : text.toCharArray()) {
                    if (c == letter) count++;
                }
                if (count > max) count = max;
                count = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + "был прерван");
        }
        return max;
    }
}