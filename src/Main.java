import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static BlockingQueue<String> lookingForA = new ArrayBlockingQueue<>(10);
    public static BlockingQueue<String> lookingForB = new ArrayBlockingQueue<>(10);
    public static BlockingQueue<String> lookingForC = new ArrayBlockingQueue<>(10);

    public static int maxA = 0;
    public static int maxB = 0;
    public static int maxC = 0;
    
    public static void main(String[] args) throws InterruptedException {
        String[] texts = new String[10_00];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = generateText("abc", 100_00);
        }

        Thread putter = new Thread(() -> {
            try {
                for (String word : texts) {
                    lookingForA.put(word);
                    lookingForB.put(word);
                    lookingForC.put(word);
                }
                lookingForA.put("end");
                lookingForB.put("end");
                lookingForC.put("end");
            } catch (InterruptedException ignored) {
            }

        });

        Thread lookingA = new Thread(() -> {
            maxA = lookingForSymbol(lookingForA, 'a', maxA);
        });

        Thread lookingB = new Thread(() -> {
            maxB = lookingForSymbol(lookingForB, 'b', maxB);
        });

        Thread lookingC = new Thread(() -> {
            maxC = lookingForSymbol(lookingForC, 'c', maxC);
        });

        putter.start();
        lookingA.start();
        lookingB.start();
        lookingC.start();

        lookingA.join();
        lookingB.join();
        lookingC.join();

        lookingA.interrupt();
        lookingB.interrupt();
        lookingC.interrupt();
        putter.interrupt();

        System.out.println("countA = " + maxA);
        System.out.println("countB = " + maxB);
        System.out.println("countC = " + maxC);
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static int lookingForSymbol(BlockingQueue<String> queue, char ch, int max) {
        try {
            String word;
            while (!(word = queue.take()).equals("end")) {
                int count = (int) word.chars()
                        .filter(a -> a == ch)
                        .count();
                if (count > max) {
                    max = count;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return max;
    }
}