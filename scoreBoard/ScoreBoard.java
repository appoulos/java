import java.math.BigInteger;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class ScoreBoard {
  static String aName = "a:";
  static String bName = "b:";
  static int minWidth = 10;
  static String prompt; // = "> "; // set in main()
  static String scoreTotFormat = "%s %" + minWidth + "d  %s %" + minWidth + "d";

  public static void helpVersion2() {
    System.out.println("# Enter one or two numbers separated by whitespace and press return.");
    System.out.println("  To add only to b's score enter a space and only one number");
    prompt = "# ";
  }

  public static void helpVersion1() {
    System.out.println("$ Enter a number and press return to add to a's score.");
    System.out.println("  To add to b's score enter whitespace (tab or a space) and a number");
    prompt = "$ ";
  }

  public static void help() {
    System.out.println("Commands:");
    System.out.println("  \"h\" or \"?\" for help");
    System.out.println("  \"q\" or \"ctrl-d\" or \"ctrl-z\" to quit");
    System.out.println("  \"r\" to reset scores");
    System.out.println("  \"s\" to switch input style (one or two numbers)");
  }

  public static void score(BigInteger a, BigInteger b) {
    int aWidth = Math.max(minWidth, a.toString().length());
    int bWidth = Math.max(minWidth, b.toString().length());

    System.out.println( "=".repeat(4 + 
          aName.length() + 
          bName.length() + 
          aWidth +
          bWidth ));
    
    System.out.printf(scoreTotFormat, aName, a, bName, b);
    System.out.print("  " + prompt);
  }

  public static void scoreAdd(BigInteger a, BigInteger addA, BigInteger b, BigInteger addB) {
    int aWidth = Math.max(minWidth, a.toString().length());
    int bWidth = Math.max(minWidth, b.toString().length());

    String scoreAddFormat = "+" + " ".repeat(aName.length()) + 
      "%" + aWidth + "d   " +
      " ".repeat(bName.length()) +
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
    BigInteger input = new BigInteger("0"); // input converted to int
    boolean addToB = false;

    Pattern pattern = Pattern.compile("(\\s*)([-0-9]*)(\\s*)([-0-9]*)"); // regex (version2)

    score(a, b);
    while (true) {
      try { // just in case of unhandled exception somewhere!
        try { // handles case when stdin is closed with ctrl-d or ctrl-z
          s = scan.nextLine();
        } catch (Exception e) {
          System.out.println();
          System.exit(0);
        }

        if (s.trim().length() > 0) {
          switch (s.trim().charAt(0)) {
            case 'h','?': help();
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

        if (useVersion2) { // version2 (regex)
          Matcher matcher = pattern.matcher(s);
          // while (matcher.find()) { // this gets a little crazy for trying to enter many numbers per line ie. "1 2 3 4"
          if (matcher.find()) {
            // debug the regex groups:
            // for (int i = 1; i <= 4; ++i) {
            //   System.out.println("group " + i + ": " + matcher.group(i) + ".");
            // }
            try {
              if (matcher.group(1).length() > 0) { // starts with whitespace so only add to b
                addA = new BigInteger("0");
                addB = new BigInteger(matcher.group(2));
              } else {
                addA = new BigInteger(matcher.group(2));
                if (matcher.group(4).length() > 0) {
                  addB = new BigInteger(matcher.group(4));
                } else {
                  addB = new BigInteger("0");
                }
              }
            } catch (NumberFormatException e) {
              System.out.print("Invalid number, ");
              addA = new BigInteger("0");
              addB = new BigInteger("0");
            }
          }
        } else { // version1 (non-regex)
          if (s.length() > 0 && Character.isWhitespace(s.charAt(0))) { 
            s = s.trim();
            addToB = true;
          } else {
            addToB = false;
          }
          try {
            input = new BigInteger(s);
          } catch (NumberFormatException e) {
            System.out.print("Invalid number, ");
            input = new BigInteger("0");
          } catch (Exception e) {
            e.printStackTrace();
          }

          if (addToB) {
            addA = new BigInteger("0");
            addB = input;
          } else {
            addA = input;
            addB = new BigInteger("0");
          }
        }

        if (addA.compareTo(BigInteger.ZERO) == 0 && addB.compareTo(BigInteger.ZERO) == 0) {
          System.out.println("No change!");
        } else {
          scoreAdd(a, addA, b, addB);
          a = a.add(addA);
          b = b.add(addB);
        }

        score(a, b);
      } catch (Exception e) { // just in case of unhandled exception somewhere!
        e.printStackTrace();
      }
      }
    }
  }
