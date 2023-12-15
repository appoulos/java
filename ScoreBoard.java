import java.math.BigInteger;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class ScoreBoard {
  static String aName = "a:";
  static String bName = "b:";
  static int width = 10;

  static String scoreAddFormat = "+" + repeat(" ",aName.length()) + 
    "%" + width + "d   " +
    repeat(" ",bName.length()) +
    "%" + width + "d\n";

  static String scoreTotFormat = "%s %" + width + "d  %s %" + width + "d";

  public static void helpVersion2() {
    System.out.println("Enter one or two numbers separated by whitespace and press return.\nTo add only to b's score enter a space and only one number");
  }

  public static void helpVersion1() {
    System.out.println("Enter a number and press return to add to a's score.\nTo add to b's score enter whitespace (tab or a space) and a number");
  }

  public static void help() {
    System.out.println("Commands:");
    System.out.println("  \"h\" for help");
    System.out.println("  \"q\" to quit");
    System.out.println("  \"r\" to reset scores");
    System.out.println("  \"s\" to switch input style (one or two numbers)");
  }

  public static String repeat(String s, int times) {
    String r = "";
    for (int i=0; i < times; i++) {
      r += s;
    }
    return r;
  }

  public static void score(BigInteger a, BigInteger b) {
    int aWidth = Math.max(width, a.toString().length());
    int bWidth = Math.max(width, b.toString().length());
    System.out.println( repeat("=", 4 + 
          aName.length() + 
          bName.length() + 
          aWidth +
          bWidth ));
    System.out.printf(scoreTotFormat, aName, a, bName, b);
    System.out.print("  $ ");
  }

  public static void scoreAdd(BigInteger a, BigInteger addA, BigInteger b, BigInteger addB) {
    int aWidth = width;
    aWidth = Math.max(aWidth, a.toString().length());
    aWidth = Math.max(aWidth, addA.toString().length());
    // System.out.println("aWidth: " + aWidth);
    int bWidth = width;
    bWidth = Math.max(bWidth, b.toString().length());
    bWidth = Math.max(bWidth, addB.toString().length());

    String scoreAddFormat = "+" + repeat(" ",aName.length()) + 
    "%" + aWidth + "d   " +
    repeat(" ",bName.length()) +
    "%" + bWidth + "d\n";
    System.out.printf(scoreAddFormat, addA, addB);

  }

  public static void main(String[] args) {
    boolean useVersion2 = false;
    help();
    if (useVersion2) {
      helpVersion2();
    } else {
      helpVersion1();
    }
    Scanner scan = new Scanner(System.in);
    BigInteger a = new BigInteger("0");
    BigInteger b = new BigInteger("0");
    BigInteger addA = new BigInteger("0");
    BigInteger addB = new BigInteger("0");
    String s = ""; // input string

    // following 2 lines are for version1
    BigInteger i = new BigInteger("0"); // input converted to int
    boolean addToB = false;

    Pattern pattern = Pattern.compile("([-0-9]*)(\\s*)([-0-9]*)"); // regex (version2)

    score(a, b);
    while (true) {
      try { // just in case of unhandled exception somewhere!
        try { // handles case when stdin is closed with ctrl-d or ctrl-z
          s = scan.nextLine();
        } catch (Exception e) {
          System.exit(0);
        }

        if (s.trim().length() > 0) {
          switch (s.trim().charAt(0)) {
            case 'h': help();
                      score(a, b);
                      continue;
            case 'q': System.exit(0);
                      break;
            case 'r': a = new BigInteger("0");
                      b = new BigInteger("0");
                      score(a, b);
                      continue;
            case 's': useVersion2 = !useVersion2;
                      if (useVersion2) {
                        helpVersion2();
                      } else {
                        helpVersion1();
                      }
                      score(a, b);
                      continue;
          }
        }

        if (useVersion2) { // regex
          Matcher matcher = pattern.matcher(s);
          // while (matcher.find()) { // this gets a little crazy for trying to enter many numbers per line i.e. "1 2 3 4"
          if (matcher.find()) {
            // System.out.println("group 1: " + matcher.group(1) + ".");
            // System.out.println("group 2: " + matcher.group(2) + ".");
            // System.out.println("group 3: " + matcher.group(3) + ".");
            if (matcher.group(1).length() > 0) {
              addA = new BigInteger(matcher.group(1));
              // addA = Integer.parseInt(matcher.group(1));
            } else {
              addA = new BigInteger("0");
            }
            if (matcher.group(3).length() > 0) {
              addB = new BigInteger(matcher.group(3));
              // addB = Integer.parseInt(matcher.group(3));
            } else {
              addB = new BigInteger("0");
            }
          }
        } else { // non-regex
          if (s.length() > 0 && Character.isWhitespace(s.charAt(0))) { 
            // == ' ' || s.charAt(0) == '\t'
            s = s.trim(); // System.out.print(", s: " + s);
            addToB = true;
          } else {
            addToB = false;
          }
          try {
            // i = Integer.parseInt(s);
            i = new BigInteger(s);
          } catch (NumberFormatException e) {
            System.out.println("Invalid number");
            // i = 0;
            i = new BigInteger("0");
          } catch (Exception e) {
            e.printStackTrace();
          }

          if (addToB) {
            addA = new BigInteger("0");
            addB = i;
          } else {
            addA = i;
            addB = new BigInteger("0");
          }
        }

        if (addA.compareTo(BigInteger.ZERO) > 0 && addB.compareTo(BigInteger.ZERO) > 0) {
          System.out.println("No change!");
        } else {
          // System.out.printf(scoreAddFormat, addA, addB);
          scoreAdd(a, addA, b, addB);
          a = a.add(addA);
          b = b.add(addB);
          // b += addB;
        }

        score(a, b);
      } catch (Exception e) { // just in case of unhandled exception somewhere!
        e.printStackTrace();
      }
      }
    }
  }
