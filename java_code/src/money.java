import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class money implements Runnable {

  static HashMap<String, Integer> customers;
  static HashMap<String, Integer> banks;
  static int numberOfCustomers;
  static int numberOfBanks;

  public money(HashMap<String, Integer> customersMap,
      HashMap<String, Integer> banksMap) {
    customers = customersMap;
    banks = banksMap;
  }

  public static void main(String[] args) {
    HashMap<String, Integer> customers = readFile("customers.txt");
    HashMap<String, Integer> banks = readFile("banks.txt");
    money money = new money(customers, banks);
    money.run();
  }

  //ref : https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
  private static HashMap<String, Integer> readFile(String filename) {
    File file = new File(filename);
    HashMap<String, Integer> data = new HashMap<>();
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
      System.out.println(customer);
      if (customer == null) {
        System.out.println("All Customer served");
        isValid = false;
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
            Thread.sleep(1000);
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

  private HashMap<String, Integer> removeProcessedCustomer(HashMap<String, Integer> customerMap) {
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

  private Customer getRandomValidCustomer(HashMap<String, Integer> customers,
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

  public static Bank getRandomBank() {
    if (banks.size() == 0) {
      return null;
    }
    Bank validBank = null;
    while (true) {
      Random random = new Random();
      int randomCustomer = random.nextInt(numberOfBanks);
      Object[] customerArray = banks.keySet().toArray();
      String name = (String) customerArray[randomCustomer];
      if (banks.get(name) > 0) {
        validBank = new Bank(name, banks.get(name));
        break;
      } else {
        continue;
      }

    }
    return validBank;
  }


}

class Transaction implements Runnable {

  static HashMap<String, Integer> banksData = new HashMap<String, Integer>();
  String requestedBank;

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  Customer customer;

  public Transaction(HashMap<String, Integer> banks) {
    this.banksData = banks;
  }

  @Override
  public void run() {
    System.out.println(customer);
    System.out.println(money.getRandomBank());
    int totalmoney = money.customers.get(customer.name);
    money.customers.put(customer.name, totalmoney - customer.loanRequested);
    System.out.println(money.customers.get(customer.name));
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
