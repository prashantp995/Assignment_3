import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class money implements Runnable {

  static HashMap<String, Integer> customers;
  static HashMap<String, Integer> banks;
  int numberOfCustomers;
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
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return data;
  }

  @Override
  public void run() {
    Customer customer = getRandomValidCustomer(customers, numberOfCustomers);
    for (String name : customers.keySet()) {
      String loanRequest = customers.get(name).toString();
      System.out.println(name + " " + loanRequest);
    }
  }

  private Customer getRandomValidCustomer(HashMap<String, Integer> customers,
      int numberOfCustomers) {
    Random random = new Random();
    int randomCustomer = random.nextInt(numberOfCustomers + 1);
    Object[] customerArray = customers.keySet().toArray();
    String name = (String) customerArray[randomCustomer];
    if (customers.get(name) > 0) {
      Customer validCustomer = new Customer(name, customers.get(name));
      return validCustomer;
    }
    return null;

  }

  private class Customer {

    int name;
    int loanRequested;

    public Customer(String name, Integer integer) {

    }
  }
}
