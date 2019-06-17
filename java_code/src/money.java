import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class money implements Runnable {

  static ConcurrentHashMap<String, Integer> customers;
  static ConcurrentHashMap<String, Integer> banks;
  static int numberOfCustomers;
  static int numberOfBanks;
  static HashMap<String, ArrayList<String>> avoidBankList = new HashMap<>();
  static boolean nobankcanserve = false;

  public money(ConcurrentHashMap<String, Integer> customersMap,
      ConcurrentHashMap<String, Integer> banksMap) {
    customers = customersMap;
    banks = banksMap;
  }

  public static void main(String[] args) {
    ConcurrentHashMap<String, Integer> customers = readFile("customers.txt");
    ConcurrentHashMap<String, Integer> banks = readFile("banks.txt");
    money money = new money(customers, banks);
    money.run();
  }

  //ref : https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
  private static ConcurrentHashMap<String, Integer> readFile(String filename) {
    File file = new File(filename);
    ConcurrentHashMap<String, Integer> data = new ConcurrentHashMap<>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String st;
      while ((st = br.readLine()) != null) {
        String[] content = st.replace("{", "").replace(".", "").replace("}", "").split(",");
        data.put(content[0], Integer.parseInt(content[1]));
        if (filename.contains("customer")) {
          numberOfCustomers = numberOfCustomers + 1;
        }
        if (filename.contains("bank")) {
          numberOfBanks = numberOfBanks + 1;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return data;
  }

  @Override
  public void run() {
    Transaction transaction = new Transaction(banks);
    boolean isValid = true;
    while (isValid) {

      Customer customer = getRandomValidCustomer(removeProcessedCustomer(customers),
          numberOfCustomers);
      if (customer == null || nobankcanserve) {
        System.out.println("All Customer served");
        isValid = false;
        for (String name : banks.keySet()) {
          String key = name.toString();
          String value = banks.get(name).toString();
          System.out.println(key + " " + value);
        }
      } else {
        if (customer.loanRequested > 0) {
          Random random = new Random();
          if (customer.loanRequested > 50) {
            int loanRequest = random.nextInt(50) + 1;
            customer.loanRequested = loanRequest;
          }
          transaction.setCustomer(customer);
          Thread thread = new Thread(transaction);
          thread.start();
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    for (String name : customers.keySet()) {
      String loanRequest = customers.get(name).toString();
      System.out.println(name + " " + loanRequest);
    }
  }

  private ConcurrentHashMap<String, Integer> removeProcessedCustomer(
      ConcurrentHashMap<String, Integer> customerMap) {
    ArrayList<String> serverdCusomters = new ArrayList<>();
    for (String customername : customerMap.keySet()) {
      if (customerMap.get(customername) == 0) {
        synchronized (this) {
          serverdCusomters.add(customername);
        }
      }
    }
    for (String servedCustomer : serverdCusomters) {
      customers.remove(servedCustomer);
      numberOfCustomers = numberOfCustomers - 1;
    }
    return customers;
  }

  private Customer getRandomValidCustomer(ConcurrentHashMap<String, Integer> customers,
      int numberOfCustomers) {
    if (customers.size() == 0) {
      return null;
    }
    Customer validCustomer = null;
    while (true) {
      Random random = new Random();
      int randomCustomer = random.nextInt(numberOfCustomers);
      Object[] customerArray = customers.keySet().toArray();
      String name = (String) customerArray[randomCustomer];
      if (customers.get(name) > 0) {
        validCustomer = new Customer(name, customers.get(name));
        break;
      } else {
        continue;
      }

    }
    return validCustomer;

  }

  public static Bank getRandomBank(String customerName) {
    if (banks.size() == 0 || allBanksHasZeroBalance() || avoidBankList.size() >= banks.size()) {
      return null;
    }
    Bank validBank = null;
    while (true) {
      Random random = new Random();
      int randomBank = random.nextInt(numberOfBanks);
      Object[] bankArray = banks.keySet().toArray();
      String name = (String) bankArray[randomBank];
      ArrayList<String> avoidedBank = new ArrayList<>();
      if (avoidBankList != null) {
        if (avoidBankList.containsKey(customerName)) {
          avoidedBank = avoidBankList.get(customerName);
        }
      }
      if (banks.get(name) > 0 && !avoidedBank.contains(name)) {
        validBank = new Bank(name, banks.get(name));
        break;
      } else {
        continue;
      }

    }
    return validBank;
  }

  private static boolean allBanksHasZeroBalance() {
    boolean result = true;
    for (String bank : banks.keySet()) {
      if (banks.get(bank) != 0) {
        result = false;
        break;
      }
    }
    return result;
  }


}

class Transaction implements Runnable {

  static ConcurrentHashMap<String, Integer> banksData = new ConcurrentHashMap<String, Integer>();
  String requestedBank;

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  Customer customer;

  public Transaction(ConcurrentHashMap<String, Integer> banks) {
    this.banksData = banks;
  }

  @Override
  public void run() {
    Bank bank = money.getRandomBank(customer.name);
    if (bank != null) {
      System.out
          .println(
              customer.name + " requested for " + customer.loanRequested + " from " + bank.name);
      int totalFund = money.banks.get(bank.name);
      if (totalFund > customer.loanRequested) {
        int totalmoney = money.customers.get(customer.name);
        money.customers.put(customer.name, totalmoney - customer.loanRequested);
        money.banks.put(bank.name, totalFund - customer.loanRequested);
        System.out.println(
            bank.name + " approved  request of " + customer.name + " for "
                + customer.loanRequested);
      } else {
        System.out.println(
            bank.name + " rejected  request of " + customer.name + " for "
                + customer.loanRequested);
        ArrayList<String> arrayList = new ArrayList<String>();
        ArrayList<String> temp;
        if (money.avoidBankList.containsKey(customer.name)) {
          temp = money.avoidBankList.get(customer.name);
          temp.add(bank.name);
          money.avoidBankList.put(customer.name, temp);
        } else {
          temp = new ArrayList<>();
          temp.add(bank.name);
          money.avoidBankList.put(customer.name, temp);
        }

      }
    } else {
      money.nobankcanserve = true;
      return;
    }


  }
}

class Customer {

  String name;
  int loanRequested;

  public Customer(String name, Integer integer) {
    this.name = name;
    this.loanRequested = integer;
  }

  @Override
  public String toString() {
    return "Customer{" +
        "name=" + name +
        ", loanRequested=" + loanRequested +
        '}';
  }


  @Override
  public int hashCode() {
    return Objects.hash(name, loanRequested);
  }
}

class Bank {

  String name;
  int fundAvailable;

  public Bank(String name, Integer integer) {
    this.name = name;
    this.fundAvailable = integer;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getFundAvailable() {
    return fundAvailable;
  }

  public void setFundAvailable(int fundAvailable) {
    this.fundAvailable = fundAvailable;
  }

  @Override
  public String toString() {
    return "Bank{" +
        "name='" + name + '\'' +
        ", fundAvailable=" + fundAvailable +
        '}';
  }
}
