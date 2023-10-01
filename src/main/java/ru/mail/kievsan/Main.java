package ru.mail.kievsan;

import java.util.*;

public class Main {
    final static String LETTERS = "RLRFR";
    final static  int ROUTE_LENGTH = 100 , AMOUNT_OF_THREADS = 1000;
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    final static List<Thread> baseThreadList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        Thread printMaxFreq = getPrintMaxFreqThread();
        printMaxFreq.start();

        for (int i = 0; i < AMOUNT_OF_THREADS; i++) {
            Thread baseThread = getSizeToFreqThread();
            baseThreadList.add(baseThread);
            baseThread.start();
        }

        // ждём, когда все основные потоки завершатся
        for (Thread thread : baseThreadList) {
            thread.join();
        }
        // и прерываем печатающий поток
        printMaxFreq.interrupt();

        System.out.println("Другие размеры: ");
        sizeToFreq
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> System.out.println(
                        " - " + e.getKey() + " (" + e.getValue() + " раз)"
                        ));
    }

    private static Thread getSizeToFreqThread() {
        return new Thread(() -> {
            String route = generateRoute(LETTERS, ROUTE_LENGTH);
            int frequency = (int) route.chars().filter(ch -> ch == 'R').count();

            synchronized (sizeToFreq) {
                sizeToFreq.put(frequency,
                        sizeToFreq.containsKey(frequency) ?
                                sizeToFreq.get(frequency) + 1 : 1);
                sizeToFreq.notify();
                // System.out.println(sizeToFreq);
            }
        });
    }

    private static Thread getPrintMaxFreqThread() {
        return new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Map.Entry<Integer, Integer> max = sizeToFreq
                            .entrySet()
                            .stream()
                            .max(Map.Entry.comparingByValue())
                            .get();
                    System.out.println(
                            "Самое частое количество повторений " + max.getKey() +
                            " (встретилось " + max.getValue() + " раз)"
                    );
                }
            }
        });
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
