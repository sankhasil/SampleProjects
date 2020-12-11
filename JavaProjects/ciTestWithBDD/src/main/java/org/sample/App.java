package org.sample;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;

public class App {

  public static void main(String[] args) throws URISyntaxException {
    processCommandLineArguments(args);

  }

  public static void processCommandLineArguments(String... args) {
    for (String arg : args) {
      switch (arg) {
        case "-h":
        case "-H":
        case "-help":
        case "-HELP":
          System.out.println("To Test AI Studio Cosy Workflow. Using CLI");
          System.exit(0);
          break;
        case "-v":
        case "-V":
        case "-version":
        case "-VERSION":
          System.out.println("1.0.0-beta");
          System.exit(0);
          break;
        default:
          break;
      }
      if (StringUtils.countMatches(arg, '=') == 1) {
        String[] splittedArg = arg.split("=");
        if (splittedArg.length == 2) {
          if (StringUtils.isBlank(splittedArg[1])) {
            System.out.println(splittedArg[0] + " key value cannot be empty.");
            System.exit(0);
          }
          switch (splittedArg[0]) {
            case "-test":
            case "test":
            case "-TEST":
            case "TEST":
            case "-t":
            case "t":
            case "-T":
            case "T":
              // TODO run splittedArg[1] named test file or test
              System.out.println("You have initiated : " + splittedArg[1]);
              break;

            default:
              break;
          }
        }
        if (splittedArg.length == 1) {
          System.out.println(splittedArg[0] + " key should have value.");
          System.exit(0);
        }
      }
    }
  }

}
