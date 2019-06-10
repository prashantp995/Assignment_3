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
  int numberOfBanks;

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
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return data;
  }

  @Override
  public void run() {
    Transaction transaction = new Transaction(banks);
    Customer customer = getRandomValidCustomer(removeProcessedCustomer(customers),
        numberOfCustomers);
    System.out.println(customer);
    if (customer == null) {
      System.out.println("All Customer served");
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

  private class Customer {

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


}

class Transaction {

  static HashMap<String, Integer> banksData = new HashMap<String, Integer>();

  public Transaction(HashMap<String, Integer> banks) {
    this.banksData = banks;

  }
}
