package gitlet;

import java.io.IOException;
import static gitlet.MyUtils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            exit("Please enter a command.");
        }


        String firstArg = args[0];
        int length = args.length;

        switch (firstArg) {
            case "init" ->
                    // TODO: handle the `init` command
                    Repository.init();

            case "add" -> {
                // TODO: handle the `add [filename]` command
                String filename = args[1];
                Repository.add(filename);
            }

            case "commit" -> {
                String message = args[1];
                String secondParent = null;
                if (length == 3) {
                    secondParent = args[2];
                }
                Repository.commit(message, secondParent);
            }

            case "rm" -> Repository.remove(args[1]);

            case "log" -> Repository.log();

            case "global-log" -> Repository.globalLog();

            case "find" -> {
                String msg = args[1];
                Repository.find(msg);
            }

            case "status" -> {
                Repository.status();
            }


            case "branch" -> {
                Repository.branch(args[1]);
            }

            case "reset" -> {
                Repository.reset(args[1]);
            }

            case "rm-branch" -> {
                String branchName = args[1];
                Repository.removeBranch(branchName);
            }

            case "checkout" -> {
                switch (args.length) {

                    case 3 -> {
                        // checkout filename
                        if (!args[1].equals("--")) {
                            MyUtils.exit("Incorrect operands.");
                        }
                        String fileName = args[2];
                        Repository.checkout(fileName);
                    }

                    case 4 -> {
                        // checkout file of given commitId
                        if (!args[2].equals("--")) {
                            MyUtils.exit("Incorrect operands.");
                        }
                        String fileName = args[3];
                        String commitId = args[1];
                        Repository.checkout(commitId, fileName);
                    }

                    case 2 -> {
                        Repository.checkoutBranch(args[1]);
                    }

                    default -> MyUtils.exit("Incorrect operands.");
                }
            }
        }
    }
}
