import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter; 
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

class History {
    private String purchaseDate; 
    private Cart cart;
    private float totalPrice;

    public void showDate() {
        System.out.println("|Date of transaction: " + purchaseDate);
    }

    // Constructor

    History(Cart cart, float totalPrice, String purchaseDate) {
        this.cart = cart.copy();
        this.totalPrice = totalPrice;
        this.purchaseDate = purchaseDate;
    }

    // Return date of purchase

    public String getDate() {
        return purchaseDate;
    }

    // Return cart object from purchase

    public Cart getCart() {
        return cart;
    }

    // Return total from purchase

    public float getPurchaseCost() {
        return Math.round(totalPrice * 100) / 100.0f;
    }
}

// Class used for product entry during purchase history restore to rebuiled cart from previous purchases

class RestoreOrderHistoryObject {
    public int id, quantity;

    public RestoreOrderHistoryObject(String id, String quantity) {
        this.id = Integer.parseInt(id);
        this.quantity = Integer.parseInt(quantity);
    }
}

// Class used for products objects that include name, category, weight, price and Id

class Product {
    private static int nextId = 1; // Static counter used to assign auto-incrementing product IDs
    private String productName, category;
    private float weight, price;
    private int id;

    Product(String name, String category, float weight, float price) {
        this.productName = name;
        this.category = category;
        this.weight = weight;
        this.id = nextId++;
        this.price = price;
    }

    // Assign given category to product

    public void assignCategory(String newCategory) {
        this.category = newCategory;
    }

    // Assign given name to product

    public void assignName(String newName) {
        this.productName = newName;
    }

    // Assign given weight to product

    public void assignWeight(float newWeight) {
        this.weight = newWeight;
    }

    // Assign given price to product

    public void assignPrice(float newPrice) {
        this.price = newPrice;
    }

    // Return product's name

    public String getName() {
        return productName;
    }

     // Return product's category

    public String getCategory() {
        return category;
    }

     // Return product's weight

    public float getWeight() {
        return weight;
    }

     // Return product's price

    public float getPrice() {
        return price;
    }

     // Return product's ID

    public int getId() {
        return id;
    }

}

class Catalog {
    
    private ArrayList<Product> products = new ArrayList<>();
    List<String> categories = new ArrayList<>(List.of("Clothes","Electronics","Food/Drinks","Misc"));
    
    
    public void showCatalog(boolean showIds) {
        System.out.println("--------- Product Catalog ---------");
        for (Product i : this.products) {
            System.out.println("----------------------");
            System.out.println(i.getName());
            System.out.print(showIds ? "Id: " + i.getId() + "\n" : "");
            System.out.println("Price: " + i.getPrice() + "€");
            System.out.println("Category: " + i.getCategory());
            System.out.println("Weight: " + ((i.getWeight() > 1000) ? i.getWeight()/1000 + "kg" : (int)i.getWeight() + "g"));
            System.out.println("----------------------");
        }
    }

    // Show catalog products with cart quantity for each product

    public void showCartCatalog(boolean showIds, Cart cart, boolean removing) {
        System.out.println("--------- Product Catalog ---------");
        for (Product i : this.products) {
            if (cart.getAmount(i.getId()) < 1 && removing) {
                continue;
            } // If user is removing products from the cart, skip products that are not in the cart
            System.out.println("----------------------");
            System.out.println(i.getName());
            System.out.print(showIds ? "Id: " + i.getId() + "\n" : "");
            System.out.println("Price: " + i.getPrice() + "€");
            System.out.println("Category: " + i.getCategory());
            System.out.println("Weight: " + ((i.getWeight() > 1000) ? i.getWeight()/1000 + "kg" : (int)i.getWeight() + "g"));
            System.out.println("(In cart: " + cart.getAmount(i.getId()) + ")");
            System.out.println("----------------------");
        }
   }

   // Function that restores catalog data from data file

   public void restoreCatalog(String FileName) {
        File dataFile = new File("Data/" + FileName);
        try {
            dataFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Unable to access catalog file. " + e.getMessage());
            return;
        }
        try (Scanner myReader = new Scanner(dataFile)) {
            while (myReader.hasNextLine()) {
                if (myReader.nextLine().equals("__New_Item__")) {
                String productName = myReader.nextLine();
                Integer categoryID = Integer.parseInt(myReader.nextLine());
                Float weight = Float.parseFloat(myReader.nextLine());
                Float price = Float.parseFloat(myReader.nextLine());
                addProduct(new Product(productName ,categories.get(categoryID), weight, price));
                }
            }
        } catch (IOException e) {
            System.out.println("Error during catalog restore. " + e.getMessage());
            return;
        }
        System.out.println("Catalog data loaded successfully. (Data file: "+dataFile.getAbsolutePath()+")");
   }

   // Function that saves catalog data into data file

   public void saveCatalog(String FileName) {
        File dataFile = new File("Data/" + FileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile))) {
            for (Product product : this.products) {
                bw.write("__New_Item__");
                bw.newLine();
                bw.write(product.getName());
                bw.newLine();
                bw.write(Integer.toString(categories.indexOf(product.getCategory())));
                bw.newLine();
                bw.write(Float.toString(product.getWeight()));
                bw.newLine();
                bw.write(Float.toString(product.getPrice()));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            System.out.println("Unable to save product from compiler to " + dataFile.getAbsolutePath() + ": " + e.getMessage());
            return;
        }
        System.out.println("Catalog data saved successfully. (Data file: "+dataFile.getAbsolutePath()+")");
   }

   // Return products from catalog

   public ArrayList<Product> getProducts() {
        return products;
   }

   // Function that adds a new product in the catalog

   public void addProduct(Product product) {
        this.products.add(product);
   }

}

class Cart {
    private ArrayList<Product> cart;
    private Catalog catalog;
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY); // Formatter used for european currency format (eg. 3.234,33€)
    
    // Constructor

    Cart(Catalog catalog) {
        this.catalog = catalog;
        this.cart = new ArrayList<>();
    }

    // Function that adds product to the user's cart with the given quantity using it's ID

    public void addProduct(int id, int quantity) {
        for (Product i : catalog.getProducts()) {
            if (i.getId() == id) {
                for (int r = 0; r < quantity; r++) {
                this.cart.add(i);
                }
            }
        }
    }

    // Function that removes product from the user's cart with the given quantity using it's ID

    public void removeProduct(int id, int quantity) {
        quantity = quantity > getAmount(id) ? getAmount(id) : quantity;
        for (int o=0; o<quantity; o++) {
            for (Product i : catalog.getProducts()) {
                if (i.getId() == id) {
                 this.cart.remove(i);
                }
            }
        }
    }

    // Returns the user's cart object

    public ArrayList<Product> getCart() {
        return this.cart;
    }

    // Returns a copy of the user's cart, since its contents are dynamic.

    public Cart copy() {
        Cart copy = new Cart(this.catalog);
        copy.cart = new ArrayList<>(this.cart);
        return copy;
    }

    // Function that returns total price of the products in the user's cart

    public float getTotalPrice() {
        float totalPrice = 0;
        for (Product product : this.getCart()) {
            totalPrice += product.getPrice();
        }
        return Math.round(totalPrice * 100) / 100.0f;
    }

    // Function that returns specific product's quantity in cart with the use of its ID

    public int getAmount(int id) {
        int quantity = 0;
        for (Product i : this.cart) {
            quantity = i.getId() == id ? quantity + 1 : quantity;
        }
        return quantity;
    }

    // Function that prints the products in the user's cart

    public void showCart() {
        ArrayList<Product> cartList = new ArrayList<>();
        for (Product i : this.cart) {
            if (!cartList.contains(i)) {
                cartList.add(i);
            }
        }
        for (Product i : cartList) {
            System.out.println("----------------------");
            System.out.println(i.getName());
            System.out.print("Quantity: ");
            System.out.println(this.getAmount(i.getId()));
            System.out.println("Price: " + (this.getAmount(i.getId()) > 1 ? i.getPrice() + " x " + this.getAmount(i.getId()) + " = " + currencyFormatter.format(i.getPrice() * this.getAmount(i.getId())) : currencyFormatter.format(i.getPrice()))) ;
            System.out.println("----------------------");
        }
        System.out.println("\n----Total price: "+currencyFormatter.format(this.getTotalPrice())+"----");
    }

    // Function that save cart data into txt file

    public void saveCart(String DataFileName) {
        File dataFile = new File("Data/"+DataFileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile))) {
            for (Product product : this.cart) {
                bw.write(Integer.toString(product.getId()));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            System.out.println("Unable to save cart to " + dataFile.getAbsolutePath() + ": " + e.getMessage());
            return;
        }
        System.out.println("Cart data saved successfully. (Data file: "+dataFile.getAbsolutePath()+")");
    }

    // Function that restores cart data from txt file

    public void restoreCart(String dataFileName) {
        File dataFile = new File("Data/"+dataFileName);
        try {
            dataFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Unable to access cart file. " + e.getMessage());
            return;
        }
        try (Scanner myReader = new Scanner(dataFile)) {
            while (myReader.hasNextLine()) {
                String nextLine = myReader.nextLine().trim();
                if (!nextLine.isEmpty() && !nextLine.equals("__OUT__")) {
                    try {
                        this.addProduct(Integer.parseInt(nextLine), 1);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid cart entry: " + nextLine);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to restore saved cart. " + e);
            return;
        }
        System.out.println("Cart data restored successfully. (Data file: "+dataFile.getAbsolutePath()+")");
    }

    // Function that clears the user's cart depending on the user's input
    
    public void clearCart() {
        Scanner input = new Scanner(System.in);
        System.out.println("Type 'Yes' to clear cart.");
        if (input.nextLine().equalsIgnoreCase("Yes")) {
            this.cart.clear();
            System.out.println("Cart cleared successfully.");
        } else {
            System.out.println("No changes were made.");
        }
    }

}



class Orders {

    ArrayList<History> purchaseHistory = new ArrayList<>();
    String[] categories = {"Clothes","Electronics","Food/Drinks","Misc"}; // Categories in a string array for loop use
    int[]    discounts  = {5,10,2,1}; // Discount percentage for each category, in the same order as the 'categories' array

    String[] promoCodes = {"M3T4M0RF0S1"}; // Valid promo codes in string array
    int[] promoDisc     = {15}; // Discount percentage for each promo code, in the same order as the 'promoCodes' array
    
    // Return category's discount value

    private int getDiscount(String category) {
        for (int i=0;i<=categories.length;i++) {
            if (categories[i] == category) {
                return discounts[i];
            }
        }
        return 0;
    }
    // Returns either 'true' or 'false' wether the user inputed code is a valid promo code or not

    private boolean checkPromo(String promoCode) {
        for (String i : promoCodes) {
            if (promoCode.equalsIgnoreCase(i)) {
                return true;
            }
        }
        return false;
    }

    // Return the promo code's discount value

    private int getPromo(String promoCode) {
        for (int i=0;i<promoCodes.length;i++) {
            if (promoCode.equalsIgnoreCase(promoCodes[i])) {
                return promoDisc[i];
            }
        }
        return 0;
    }

    // Summarize products in cart and calculate total price after discounts

    public void checkout(Cart cart) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        float totalPrice = cart.getTotalPrice();
        float delivery = 0;
        cart.showCart();
        for (Product i : cart.getCart()) {
            if (getDiscount(i.getCategory()) > 0){
                System.out.println("Product: "+ i.getName());
                System.out.println("-"+getDiscount(i.getCategory())+"% " + i.getCategory() + " purchases;");
                System.out.println("Reduction: "+currencyFormatter.format(i.getPrice() * getDiscount(i.getCategory())/100));
                totalPrice -= i.getPrice() * getDiscount(i.getCategory()) / 100;
                System.out.println("New total after discount: " + currencyFormatter.format(totalPrice) + "\n");
            }
            delivery += (i.getWeight() / 1000.0f) * 0.95f;
        }
        if (totalPrice > 1000) {
            delivery = 0;
            System.out.println("No delivery cost (Cart total over 1,000€)");
        } else {
        System.out.println("Delivery cost: " + currencyFormatter.format(delivery));
        }
        System.out.println("Do you wish to use a promo Code? 'Yes/No': ");
        Scanner answer = new Scanner(System.in);
        if (answer.next().equalsIgnoreCase("yes")) {
            System.out.print("Please enter a valid promo code (Input 'checkout' if you don't wish to use a code): ");
            Scanner input = new Scanner(System.in);
            String inputedCode = input.next();
            while (!inputedCode.equals("checkout")&& !checkPromo(inputedCode)) {
                System.out.print("Promo code doesn't exist Please enter a valid promo code: ");
                input = new Scanner(System.in);
                inputedCode = input.nextLine();
            }
            if (!inputedCode.equals("checkout")) {
               
                float discount = getPromo(inputedCode);
                System.out.println("\nValid code!");
                System.out.println("---------------------------------");
                System.out.println("Used code: " + inputedCode.toUpperCase());
                System.out.println("Discount: -" + discount + "%");
                System.out.println("---------------------------------");
                totalPrice /= ((discount+100)/100);
                totalPrice = Math.round(totalPrice * 100.0f) / 100.0f;
                System.out.println("New Total price: " + currencyFormatter.format(totalPrice + delivery));
            }
        }
        System.out.print("Continue with checkout? (Yes/No): ");
            answer = new Scanner(System.in);
            if (answer.nextLine().equalsIgnoreCase("Yes")) {
                purchaseHistory.add(new History(cart, totalPrice + delivery, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                cart.getCart().clear();
            }
    }

    // Show purchase history

    public void showHistory() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        if (purchaseHistory.isEmpty()) {
            System.out.println("No purchases yet.");
            return;
        }
        for (int i = 0; i < purchaseHistory.size(); i++) {
            System.out.println("\n -----------------------------------");
            System.out.println("|Purchase ID: "+(i+1));
            purchaseHistory.get(i).showDate();
            System.out.println(" -----------------------------------");
            System.out.println("\n Products in cart:\n");
            purchaseHistory.get(i).getCart().showCart();
            System.out.println("\n-----------------------------------");
            System.out.println("Total price after discounts: "+currencyFormatter.format(purchaseHistory.get(i).getPurchaseCost()));
            System.out.println("-----------------------------------");
            System.out.println("///////////////////////////////////");
        }
    }

    // Restore purchase history with the given data file 

    public void restoreHistory(String dataFileName, Catalog catalog) {
        File dataFile = new File("Data/"+dataFileName);
        if (!dataFile.exists()) {
            return;
        }
        try (Scanner myReader = new Scanner(dataFile)) {
            while (myReader.hasNextLine()) {
                String purchaseDate = myReader.nextLine();
                if (!myReader.hasNextLine()) {
                    break;
                }

                ArrayList<RestoreOrderHistoryObject> historyProductList = new ArrayList<>();
                String nextLine = myReader.nextLine();
                while (!nextLine.equals("__OUT__")) {
                    if (!myReader.hasNextLine()) {
                        nextLine = null;
                        break;
                    }
                    String quantity = myReader.nextLine();
                    historyProductList.add(new RestoreOrderHistoryObject(nextLine, quantity));
                    if (!myReader.hasNextLine()) {
                        nextLine = null;
                        break;
                    }
                    nextLine = myReader.nextLine();
                }
                if (nextLine == null || !myReader.hasNextLine()) {
                    break;
                }

                Cart oldCart = new Cart(catalog);
                for (RestoreOrderHistoryObject product : historyProductList) {
                    oldCart.addProduct(product.id, product.quantity);
                }
                String finalPrice = myReader.nextLine();
                purchaseHistory.add(new History(oldCart, Float.parseFloat(finalPrice), purchaseDate));
            }
        } catch (IOException e) {
            System.out.println("Error occurred while restoring purchase history data. " + e.getMessage());
            return;
        }
        System.out.println("Purchase history data restored successfully. (Data file:"+dataFile.getAbsolutePath()+")");
    }

    // Save history purchase data to the given data file

    public void saveHistory(String DataFileName, Catalog catalog) {
        // Save purchase history data on txt file
        File dataFile = new File("Data/"+DataFileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("Data/"+DataFileName))) {
            for (History record : purchaseHistory) {
                bw.write(record.getDate());
                bw.newLine();
                ArrayList<Product> cartList = new ArrayList<>();
                for (Product i : record.getCart().getCart()) {
                    if (!cartList.contains(i)) {
                        cartList.add(i);
                    }
                }
                for (Product i : cartList) {
                    bw.write(Integer.toString(i.getId()));
                    bw.newLine();
                    bw.write(Integer.toString(record.getCart().getAmount(i.getId())));
                    bw.newLine();
                }
                bw.write("__OUT__");
                bw.newLine();
                bw.write(Float.toString(record.getPurchaseCost()));
                bw.newLine();
            }
        } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("Logs/"+DataFileName))) {
            for (History record : purchaseHistory) {
                bw.newLine();
                bw.write("Purchase Date: " + record.getDate());
                bw.newLine();
                ArrayList<Product> cartList = new ArrayList<>();
                for (Product i : record.getCart().getCart()) {
                    if (!cartList.contains(i)) {
                        cartList.add(i);
                    }
                }
                for (Product i : cartList) {
                    bw.write("-------------");
                    bw.newLine();
                    bw.write("Product: " + i.getName());
                    bw.newLine();
                    bw.write("Price: " + i.getPrice() + "€");
                    bw.newLine();
                    bw.write("Quantity: " + Integer.toString(record.getCart().getAmount(i.getId())));
                    bw.newLine();
                    bw.write("-------------");
                    bw.newLine();
                }
                bw.newLine();
                bw.write("Total: " + Float.toString(record.getPurchaseCost()) + "€");
                bw.newLine();
            }
        } catch (IOException e) {
                System.out.println("Unable to saved purchase history log.");
                e.printStackTrace();
                return;
            }
        System.out.println("Purchase history log saved successfully. (Data file: "+dataFile.getAbsolutePath()+")");
    }
}

public class Main {

    // Company details

    String companyName = "NEXT LEVEL SHOPPING SOLUTIONS SMPC";
    String vat = "EL090000045";
    String adress = "328 Vouliagmenis Avenue, Agios Dimitrios, 173 43, Athens, Greece";
    String activity = "Retail Sale of Computers, Electronics & Consumer Goods via Internet (NACE 47.91)";
    String taxOffice = "KEFODE Attikis (Attica Tax Operations Center)";
    String tel = "+30 210 000 0000";
    String mail = "info@nlss.gr";

    // Categories in a string array

    String[] categories = {"Clothes","Electronics","Food/Drinks","Misc"};

    // Currency formatter method for price format in euro (eg. 45.233,30€)
    
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    // Function that adds selected products with the given quantities to the cart

    public void add_product_to_cart(Cart cart, Catalog catalog) {
        catalog.showCartCatalog(true, cart, false);
        while (true) {
        boolean found = false;
        System.out.print("Input id of the product or type 'Exit' to go back: ");
        Scanner input = new Scanner(System.in);
        String inputedId = input.nextLine();
        if (inputedId.equalsIgnoreCase("Exit")) {
            break;
        }
            for (Product i : catalog.getProducts()) {
                if (inputedId.equals(""+i.getId()+"")) {
                    found = true;
                    System.out.println("--------------------------------------------------");
                    System.out.println("ID: "+i.getId()+" matched with product "+i.getName());
                    System.out.print("Please input quantity: ");
                    int inputQuantity = input.nextInt();
                    cart.addProduct(i.getId(), Math.abs(inputQuantity));
                    System.out.println("\n----------------------------------------------------");
                    if (cart.getAmount(i.getId()) == 1) {
                        System.out.println(i.getName() + " priced at " + i.getPrice() + "€" + " added to shopping cart");
                    } 
                    else {
                        System.out.println(i.getName() + " (x"+cart.getAmount(i.getId())+") priced at " + i.getPrice() + "€" + " added to shopping cart");
                    }
                    System.out.println("New total at: " + cart.getTotalPrice());
                    System.out.println("----------------------------------------------------\n");
                }
            }
        if (!found) {
            System.out.println("Invalid ID.");
        }
    }
    

   }

    // Function that adds user-created products on the current catalog

    public void add_product_to_catalog(Catalog catalog) {
        while (true) {
        boolean found = false;
        System.out.print("Input name of the product you wish to add or type 'Exit' to go back: ");
        Scanner input = new Scanner(System.in);
        String inputedName = input.nextLine();
        if (inputedName.equalsIgnoreCase("Exit")) {
            break;
        }
        for (Product i : catalog.getProducts()) {
            if (inputedName.equals(i.getName())) {
                found = true;
            }
        }
        if (found) {
            System.out.println("Product with that name already exists.");
            continue;
        }
        System.out.println("Choose a category: ");
        int count = 1;
        for (String i : categories) {
            System.out.println(count+". "+i);
            count += 1;
        }
        int category = input.nextInt();
        System.out.println("Input price: ");
        float price = input.nextFloat();
        System.out.println("Input weight (g): ");
        float weight = input.nextFloat();
        System.out.println("----------------------");
        System.out.println(inputedName);
        System.out.println("Price: " + currencyFormatter.format(price));
        System.out.println("Category: " + categories[category-1]);
        System.out.println("Weight: " + ((weight > 1000) ? weight/1000 + "kg" : (int)weight + "g"));
        System.out.println("----------------------");
        input.nextLine(); // Consume the newline left by nextFloat().
        System.out.print("Do you wish to proceed? (Yes/No): ");
        String answer = input.nextLine();
        if (answer.equalsIgnoreCase("No")) {
            continue;
        }
        catalog.addProduct(new Product(inputedName,categories[category-1],weight,price));
        System.out.print("Do you wish to create a new product? (Yes/No): ");
        answer = input.nextLine();
        if (answer.equalsIgnoreCase("no")) {
            break;
        }
    }
    

   }

    // Function that removes selected products from the cart

    public void remove_product_from_cart(Cart cart, Catalog catalog) {
        catalog.showCartCatalog(true, cart, true);
        while (true) {
        boolean found = false;
        System.out.print("Input id of the product or type 'Exit' to go back: ");
        Scanner input = new Scanner(System.in);
        String inputedId = input.nextLine();
        if (inputedId.equalsIgnoreCase("Exit")) {
            break;
        }
            for (Product i : catalog.getProducts()) {
                if (inputedId.equals(""+i.getId()+"") && cart.getAmount(i.getId()) > 0) {
                    found = true;
                    Integer productAmountInCart = cart.getAmount(i.getId());
                    System.out.println("--------------------------------------------------");
                    System.out.println("ID: "+i.getId()+" matched with product "+i.getName());
                    System.out.print("Please input quantity you wish to remove: ");
                    int inputQuantity = input.nextInt();
                    cart.removeProduct(i.getId(), Math.abs(inputQuantity));
                    System.out.println("\n----------------------------------------------------");
                    if (inputQuantity >= productAmountInCart) {
                        System.out.println("All quantities of the product removed from the cart");
                    }
                    else {
                        System.out.println(inputQuantity + "x "+i.getName()+" removed from cart ("+cart.getAmount(i.getId())+" remaining)");
                    }
                    System.out.println("New total at: " + cart.getTotalPrice() + "€");
                    System.out.println("----------------------------------------------------\n");
                }
            }
        if (!found) {
            System.out.println("Invalid ID.");
        }
    }

   }

    // Function that displays the e-shop menu options

    public void showMenu(Catalog catalog, Cart cart, Orders history, String dataFileName, String cartDataFileName, String catalogFileName) {
        boolean exit = false;
        while (!exit) {
        System.out.println("\n 1. Show catalog.");
        System.out.println(" 2. Add products to cart.");
        System.out.println(" 3. Remove products from cart.");
        System.out.println(" 4. Show products in cart.");
        System.out.println(" 5. Clear cart");
        System.out.println(" 6. Create product");
        System.out.println(" 7. Checkout");
        System.out.println(" 8. Purchase history log");
        System.out.println(" 9. Company details");
        System.out.println("10. Exit e-shop");
        System.out.print("\nInput an option: "); 
        Scanner input = new Scanner(System.in);
        int option = input.nextInt();
        switch (option) {
            case 1:
                catalog.showCatalog(false);
                break;
            case 2:
                add_product_to_cart(cart, catalog);
                break;
            case 3:
                remove_product_from_cart(cart, catalog);
                break;
            case 4:
                System.out.println("\n------------------------------------------\n           ||Products in cart||\n------------------------------------------");
                cart.showCart();
                break;
            case 5:
                cart.clearCart();
                break;
            case 6:
                add_product_to_catalog(catalog);
                break;
            case 7:
                history.checkout(cart);
                break;
            case 8:
                history.showHistory();
                break;
            case 9:
                System.out.println("-------------------");
                System.out.println("Company Details:\n");
                System.out.println("Company Name: " + companyName);
                System.out.println("Business Activity: " + activity);
                System.out.println("VAT Number: " + vat);
                System.out.println("Tax Office: " + taxOffice);
                System.out.println("Address: " + adress);
                System.out.println("Tel:" + tel);
                System.out.println("Email: " + mail);
                System.out.println("-------------------");
                break;
            case 10:
                System.out.println("NLSS log: Saving data...");
                cart.saveCart(cartDataFileName);
                history.saveHistory(dataFileName, catalog);
                catalog.saveCatalog(catalogFileName);
                exit = true;
                break;
            default:
                System.out.println("Not a valid option.");  
            }
        }
    }

    // Application entry point: restore saved data, menu initialization.
    
    public static void main(String[] args) {
        Main shop = new Main(); // Initialize shop app
        Catalog catalog = new Catalog(); // Initialize catalog array
        String historyFileLogName = "historyLog.txt"; // Name of the file used for data and log txt
        String cartFileLogName = "cartLog.txt"; // Name of the file used for cart data saving
        String catalogFileName = "catalog.txt"; // Name of the file used for catalog data saving
        
        catalog.restoreCatalog(catalogFileName); // Load catalog data from given txt file
       
        Cart cart = new Cart(catalog); // Initialize cart with the given catalog data (dynamic)
        
        Orders history = new Orders(); // Initialize history object 
        history.restoreHistory(historyFileLogName, catalog); // Load purchase history data from given txt file
        
        cart.restoreCart(cartFileLogName); // Load cart data from given txt file

        System.out.println("\n__|| NLSS E-Shop ||__"); // Display E-shop brand name

        shop.showMenu(catalog, cart, history, historyFileLogName, cartFileLogName, catalogFileName); // Show menu options
    }
}