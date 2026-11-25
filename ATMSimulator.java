import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class ATMSimulator {

    private static final String FILE_NAME = "accounts.txt";

    // Available ATM currency notes, in descending order.
    private static final int[] DENOMINATIONS = {500, 200, 100};

    // Queue: customers are served in FIFO order.
    private static final Queue<String> customerQueue = new LinkedList<>();

    // Account collection: accounts are searched using linear searching.
    private static final ArrayList<Account> accounts = new ArrayList<>();

    private static final Scanner sc = new Scanner(System.in);

    static class Account {
        String accountNumber;
        String name;
        String pin;
        int balance;
        boolean blocked;

        // Stack: stores transactions; the latest transaction is shown first.
        Stack<String> transactions = new Stack<>();

        Account(String accountNumber, String name, String pin, int balance) {
            this.accountNumber = accountNumber;
            this.name = name;
            this.pin = pin;
            this.balance = balance;
            this.blocked = false;
        }
    }

    public static void main(String[] args) {
        loadAccounts();

        if (accounts.isEmpty()) {
            System.out.println("No valid accounts found in " + FILE_NAME + ".");
            sc.close();
            return;
        }

        // Add all loaded customers to the waiting queue.
        for (Account account : accounts) {
            customerQueue.offer(account.accountNumber);
        }

        System.out.println("======================================");
        System.out.println("            ATM SIMULATOR");
        System.out.println("======================================");

        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Serve next customer");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    serveNextCustomer();
                    break;

                case 2:
                    saveAccounts();
                    System.out.println("Account data saved successfully.");
                    System.out.println("Thank you for using ATM Simulator.");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid menu option. Please enter 1 or 2.");
            }
        }
    }

    // Removes and serves the next customer from the Queue.
    private static void serveNextCustomer() {
        String accountNumber = customerQueue.poll();

        if (accountNumber == null) {
            System.out.println("No customers are waiting in the queue.");
            return;
        }

        Account account = searchAccount(accountNumber);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        if (account.blocked) {
            System.out.println("This card is blocked.");
            return;
        }

        System.out.println("\nNow serving: " + account.name
                + " (" + account.accountNumber + ")");

        if (login(account)) {
            customerMenu(account);
        }
    }

    // Allows three PIN attempts. The card is blocked after three wrong PINs.
    private static boolean login(Account account) {
        int attemptsLeft = 3;

        while (attemptsLeft > 0) {
            System.out.print("Enter PIN: ");
            String enteredPin = sc.nextLine().trim();

            if (account.pin.equals(enteredPin)) {
                System.out.println("Login successful. Welcome, " + account.name + ".");
                System.out.println("Balance: " + account.balance);
                return true;
            }

            attemptsLeft--;

            if (attemptsLeft > 0) {
                System.out.println("Incorrect PIN. Attempts left: " + attemptsLeft);
            } else {
                account.blocked = true;
                System.out.println("Incorrect PIN. Your card is blocked after 3 wrong attempts.");
            }
        }

        return false;
    }

    // Customer menu numbering follows the supplied project test cases.
    private static void customerMenu(Account account) {
        while (true) {
            System.out.println("\n========== CUSTOMER MENU ==========");
            System.out.println("2. Balance enquiry");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Mini statement");
            System.out.println("6. Logout");
            System.out.print("Enter menu option: ");

            int choice = readInt();

            switch (choice) {
                case 2:
                    showBalance(account);
                    break;

                case 3:
                    deposit(account);
                    break;

                case 4:
                    withdraw(account);
                    break;

                case 5:
                    showMiniStatement(account);
                    break;

                case 6:
                    System.out.println("Customer logged out successfully.");
                    return;

                default:
                    System.out.println("Invalid menu option.");
            }
        }
    }

    private static void showBalance(Account account) {
        System.out.println("Balance: " + account.balance);
    }

    // Deposit operation. The project statement does not restrict deposits
    // to multiples of 100, so any positive whole-number amount is accepted.
    private static void deposit(Account account) {
        System.out.print("Enter deposit amount: ");
        int amount = readInt();

        if (amount <= 0) {
            System.out.println("Deposit amount must be greater than zero.");
            return;
        }

        account.balance += amount;
        account.transactions.push(
                "Deposit " + amount + " | Balance " + account.balance
        );

        System.out.println("Deposit successful. Balance: " + account.balance);
    }

    // Withdrawal uses the Greedy Algorithm: largest note first.
    private static void withdraw(Account account) {
        System.out.print("Enter withdrawal amount: ");
        int amount = readInt();

        if (amount <= 0) {
            System.out.println("Withdrawal amount must be greater than zero.");
            return;
        }

        if (amount % 100 != 0) {
            System.out.println("Withdrawal amount must be a multiple of 100.");
            return;
        }

        if (amount > account.balance) {
            System.out.println("Insufficient balance. Balance: " + account.balance);
            return;
        }

        int remainingAmount = amount;
        int[] noteCount = new int[DENOMINATIONS.length];

        for (int i = 0; i < DENOMINATIONS.length; i++) {
            noteCount[i] = remainingAmount / DENOMINATIONS[i];
            remainingAmount = remainingAmount % DENOMINATIONS[i];
        }

        if (remainingAmount != 0) {
            System.out.println("ATM cannot dispense this amount.");
            return;
        }

        account.balance -= amount;
        account.transactions.push(
                "Withdraw " + amount + " | Balance " + account.balance
        );

        System.out.println("Dispensed:");
        for (int i = 0; i < DENOMINATIONS.length; i++) {
            if (noteCount[i] > 0) {
                System.out.println(DENOMINATIONS[i] + " x " + noteCount[i]);
            }
        }

        System.out.println("Balance: " + account.balance);
    }

    // Stack is traversed from top to bottom: latest transaction first.
    private static void showMiniStatement(Account account) {
        if (account.transactions.empty()) {
            System.out.println("No transactions available.");
            return;
        }

        System.out.println("\n===== MINI STATEMENT (LATEST FIRST) =====");

        int displayed = 0;
        for (int i = account.transactions.size() - 1;
             i >= 0 && displayed < 5; i--) {

            System.out.println((displayed + 1) + ". "
                    + account.transactions.get(i));
            displayed++;
        }
    }

    // Linear search for an account number.
    private static Account searchAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.accountNumber.equalsIgnoreCase(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    // Loads account records from accounts.txt at program start.
    private static void loadAccounts() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("ACC101,Riya,1234,5000");
                System.out.println("Sample accounts.txt created.");
            } catch (IOException e) {
                System.out.println("Unable to create account file: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (data.length != 4) {
                    System.out.println("Skipping invalid account record: " + line);
                    continue;
                }

                try {
                    String accountNumber = data[0].trim();
                    String name = data[1].trim();
                    String pin = data[2].trim();
                    int balance = Integer.parseInt(data[3].trim());

                    if (accountNumber.isEmpty() || name.isEmpty()
                            || pin.isEmpty() || balance < 0) {
                        System.out.println("Skipping invalid account record: " + line);
                        continue;
                    }

                    accounts.add(new Account(accountNumber, name, pin, balance));

                } catch (NumberFormatException e) {
                    System.out.println("Skipping record with invalid balance: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading accounts: " + e.getMessage());
        }
    }

    // Saves updated account balances when the user exits.
    private static void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Account account : accounts) {
                writer.println(account.accountNumber + ","
                        + account.name + ","
                        + account.pin + ","
                        + account.balance);
            }
        } catch (IOException e) {
            System.out.println("Error saving account data: " + e.getMessage());
        }
    }

    // Reads a whole number and handles non-numeric input safely.
    private static int readInt() {
        while (true) {
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid whole number: ");
            }
        }
    }
}
