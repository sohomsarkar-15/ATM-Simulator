ATM Simulator — Rungta International Skills University
Problem Statement No. 7

Files:
- ATMSimulator.java : complete Java source code
- accounts.txt      : sample account record

Run from this folder:
    javac ATMSimulator.java
    java ATMSimulator

Sample account:
    Account number: ACC101
    Name: Riya
    PIN: 1234
    Balance: 5000

Menu mapping from the supplied project description:
Main menu:
    1 = Serve next customer
    2 = Exit

Customer menu:
    2 = Balance enquiry
    3 = Deposit
    4 = Withdraw
    5 = Mini statement
    6 = Logout

Test case notes:
1. Start with ACC101,Riya,1234,5000; choose main menu 1; enter PIN 1234.
2. Set the account balance in accounts.txt to 500 before starting; serve customer;
   enter PIN 1234; choose customer menu 4; withdraw 500.
3. Start with the sample account; choose main menu 1; enter PIN 0000 once.
   Expected: Incorrect PIN. Attempts left: 2.
4. Start with the sample account; serve customer; enter PIN 1234; choose 4 and
   withdraw 1800; choose 3 and deposit 700; choose 5 for mini statement.

Important:
- Reset accounts.txt to the required starting balance before each independent test.
- The transaction Stack is in memory for the current program run. Account records
  and updated balances are saved to accounts.txt when Exit is selected.
- Card blocking after three wrong PIN entries is maintained during the current run.
